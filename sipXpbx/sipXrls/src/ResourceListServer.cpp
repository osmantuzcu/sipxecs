// 
// Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.  
// Contributors retain copyright to elements licensed under a Contributor Agreement.
// Licensed to the User under the LGPL license.
// 
// $$
//////////////////////////////////////////////////////////////////////////////

// SYSTEM INCLUDES
// APPLICATION INCLUDES

#include <os/OsSysLog.h>
#include <os/OsDefs.h>
#include <sipdb/SubscriptionDB.h>
#include "ResourceListServer.h"

// EXTERNAL FUNCTIONS
// EXTERNAL VARIABLES
// CONSTANTS

// URN for the xmlns attribute for Resource List Meta-Information XML.
#define RLMI_XMLNS "urn:ietf:params:xml:ns:rlmi"
// MIME information for RLMI XML.
#define RLMI_CONTENT_TYPE "application/rlmi+xml"

// STATIC VARIABLE INITIALIZATIONS

const UtlContainableType ResourceListServer::TYPE = "ResourceListServer";


/* //////////////////////////// PUBLIC //////////////////////////////////// */

/* ============================ CREATORS ================================== */

// Constructor
ResourceListServer::ResourceListServer(const UtlString& domainName,
                                       const char* eventType,
                                       const char* contentType,
                                       int tcpPort,
                                       int udpPort,
                                       int tlsPort,
                                       const UtlString& bindIp,
                                       UtlString* resourceListFile,
                                       int refreshInterval,
                                       int resubscribeInterval,
                                       int publishingDelay,
                                       int maxRegSubscInResource,
                                       int maxContInRegSubsc,
                                       int maxResInstInCont,
                                       int maxDialogsInResInst) :
   mDomainName(domainName),
   mEventType(eventType),
   mContentType(contentType),
   mRefreshInterval(refreshInterval),
   mResubscribeInterval(resubscribeInterval),
   mPublishingDelay(publishingDelay),
   mMaxRegSubscInResource(maxRegSubscInResource),
   mMaxContInRegSubsc(maxContInRegSubsc),
   mMaxResInstInCont(maxResInstInCont),
   mMaxDialogsInResInst(maxDialogsInResInst),
   mServerUserAgent(
      tcpPort, // sipTcpPort
      udpPort, // sipUdpPort
      tcpPort, // sipTlsPort
      NULL, // publicAddress
      NULL, // defaultUser
      bindIp, // defaultSipAddress
      NULL, // sipProxyServers
      NULL, // sipDirectoryServers
      NULL, // sipRegistryServers
      NULL, // authenticationScheme
      NULL, // authenicateRealm
      NULL, // authenticateDb
      NULL, // authorizeUserIds
      NULL, // authorizePasswords
      NULL, // natPingUrl
      0, // natPingFrequency
      "PING", // natPingMethod
      NULL, // lineMgr
      SIP_DEFAULT_RTT, // sipFirstResendTimeout
      TRUE, // defaultToUaTransactions
      -1, // readBufferSize
      OsServerTask::DEF_MAX_MSGS, // queueSize
      FALSE // bUseNextAvailablePort
      ),
   mClientUserAgent(
      PORT_DEFAULT, // sipTcpPort
      PORT_DEFAULT, // sipUdpPort
      PORT_DEFAULT, // sipTlsPort
      NULL, // publicAddress
      NULL, // defaultUser
      bindIp, // defaultSipAddress
      NULL, // sipProxyServers
      NULL, // sipDirectoryServers
      NULL, // sipRegistryServers
      NULL, // authenticationScheme
      NULL, // authenicateRealm
      NULL, // authenticateDb
      NULL, // authorizeUserIds
      NULL, // authorizePasswords
      NULL, // natPingUrl
      0, // natPingFrequency
      "PING", // natPingMethod
      NULL, // lineMgr
      SIP_DEFAULT_RTT, // sipFirstResendTimeout
      TRUE, // defaultToUaTransactions
      -1, // readBufferSize
      OsServerTask::DEF_MAX_MSGS, // queueSize
      FALSE // bUseNextAvailablePort
      ),
   mSubscriptionMgr(SUBSCRIPTION_COMPONENT_RLS, mDomainName),
   mSubscribeServer(mServerUserAgent, mEventPublisher, mSubscriptionMgr,
                    mPolicyHolder),
   mRefreshMgr(mClientUserAgent, mDialogManager),
   mSubscribeClient(mClientUserAgent, mDialogManager, mRefreshMgr),
   mResourceListTask(this),
   mResourceListSet(this),
   // Do not set the resource list file name yet, so the ResourceListFileReader
   // doesn't add elements to the ResourceListSet before we have the
   // SIP tasks set up.
   // (The following statement takes the address of a temporary UtlString,
   // but that is OK because the pointer to the string is not kept
   // by the constructor we pass it to.)
   mResourceListFileReader(&UtlString(""), &mResourceListSet)
{
   OsSysLog::add(FAC_RLS, PRI_DEBUG,
                 "ResourceListServer:: this = %p, mDomainName = '%s', mEventType = '%s', mContentType = '%s', mRefreshInterval = %d, mResubscribeInterval = %d",
                 this, mDomainName.data(),
                 mEventType.data(), mContentType.data(),
                 mRefreshInterval, mResubscribeInterval);
   OsSysLog::add(FAC_RLS, PRI_DEBUG,
                 "ResourceListServer:: this = %p, mPublishingDelay = %d, mMaxRegSubscInResource = %d, mMaxContInRegSubsc = %d, mMaxResInstInCont = %d, mMaxDialogsInResInst = %d",
                 this,
                 publishingDelay, mMaxRegSubscInResource, mMaxContInRegSubsc,
                 mMaxResInstInCont, mMaxDialogsInResInst);

   // Initialize the call processing objects.

   // Construct addresses:
   // our local host-part
   // contact address to be used in client outgoing requests (primarily SUBSCRIBEs)
   {
      // Get our address and the client port.
      UtlString localAddress;
      int localPort;
      mClientUserAgent.getLocalAddress(&localAddress, &localPort);

      char buffer[100];

      // Construct the server's host-part.
      sprintf(buffer, "%s:%d", localAddress.data(),
              portIsValid(udpPort) ? udpPort : tcpPort);
      mServerLocalHostPart = buffer;

      // Construct the client's URI.
      sprintf(buffer, "sip:sipXrls@%s:%d", localAddress.data(), localPort);
      mClientOutgoingAddress = buffer;
   }

   // Initialize the SipUserAgent's.
   // Set the user-agent strings.
   mServerUserAgent.setUserAgentHeaderProperty("sipX/rls");
   mClientUserAgent.setUserAgentHeaderProperty("sipX/rls");
   // Allow the "eventlist" extension in the server.
   mServerUserAgent.allowExtension("eventlist");
   // Start them.
   mServerUserAgent.start();
   mClientUserAgent.start();

   // Set up the SIP Subscribe Client
   mRefreshMgr.start();
   mSubscribeClient.start();  

   // Start the ResourceListTask.
   mResourceListTask.start();

   // Start the ResourceListFileReader by giving it the file name.
   mResourceListFileReader.setFileName(resourceListFile);

   // Start the SIP Subscribe Server after the ResourceListFileReader is
   // done loading the configuration.  This ensures that early subscribers
   // do not get NOTIFYs with incomplete information.
   mSubscribeServer.enableEventType(mEventType);
   mSubscribeServer.start();
}


// Destructor
ResourceListServer::~ResourceListServer()
{
   // Close down the call processing objects.

   // Stop the SipSubscribeClient, so callbacks are no longer activated.
   mSubscribeClient.endAllSubscriptions();
   
   // Finalize ResourceListSet, so timers stop queueing messages to
   // ResourceList Task and there are no references to the
   // ResourceCached's.
   mResourceListSet.finalize();

   // Stop the SipUserAgent's.
   mServerUserAgent.shutdown(TRUE);
   mClientUserAgent.shutdown(TRUE);
}

/* ============================ MANIPULATORS ============================== */

// Shut down the server.
void ResourceListServer::shutdown()
{
   OsSysLog::add(FAC_RLS, PRI_DEBUG,
                 "ResourceListServer::shutdown this = %p",
                 this);

   // Probably best not to delete the resources in the resource lists so that
   // we do not send NOTIFYs to the clients showing the lists as empty.
   // Instead, we force the subscribe server to just tell the subscribers
   // that their subscriptions are terminated.

   // Stop the SIP subscribe client.
   mSubscribeClient.requestShutdown();
   // Stop the refresh manager.
   mRefreshMgr.requestShutdown();
   // Stop the subscribe server.
   mSubscribeServer.requestShutdown();

   // Shut down SipUserAgent's
   // Shut down the ResourceListTask
   mServerUserAgent.shutdown(FALSE);
   mClientUserAgent.shutdown(FALSE);
   mResourceListTask.requestShutdown();
   while(!(mServerUserAgent.isShutdownDone() &&
           mClientUserAgent.isShutdownDone() &&
           mResourceListTask.isShutDown()))
   {
      OsTask::delay(100);
   }
}

/* ============================ ACCESSORS ================================= */

/* //////////////////////////// PUBLIC //////////////////////////////////// */

/* ============================ INQUIRY =================================== */

/**
 * Get the ContainableType for a UtlContainable-derived class.
 */
UtlContainableType ResourceListServer::getContainableType() const
{
   return ResourceListServer::TYPE;
}

/* //////////////////////////// PROTECTED ///////////////////////////////// */

/* //////////////////////////// PRIVATE /////////////////////////////////// */


/* ============================ FUNCTIONS ================================= */
