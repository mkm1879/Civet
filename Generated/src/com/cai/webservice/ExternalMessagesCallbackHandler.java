
/**
 * ExternalMessagesCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:33:49 IST)
 */

    package com.cai.webservice;

    /**
     *  ExternalMessagesCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class ExternalMessagesCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public ExternalMessagesCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public ExternalMessagesCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for civetGetPremises method
            * override this method for handling normal response from civetGetPremises operation
            */
           public void receiveResultcivetGetPremises(
                    com.cai.webservice.ExternalMessagesStub.CivetGetPremisesResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from civetGetPremises operation
           */
            public void receiveErrorcivetGetPremises(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for civetGetLookup method
            * override this method for handling normal response from civetGetLookup operation
            */
           public void receiveResultcivetGetLookup(
                    com.cai.webservice.ExternalMessagesStub.CivetGetLookupResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from civetGetLookup operation
           */
            public void receiveErrorcivetGetLookup(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for addExternalMessage method
            * override this method for handling normal response from addExternalMessage operation
            */
           public void receiveResultaddExternalMessage(
                    com.cai.webservice.ExternalMessagesStub.AddExternalMessageResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from addExternalMessage operation
           */
            public void receiveErroraddExternalMessage(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for civetGetVets method
            * override this method for handling normal response from civetGetVets operation
            */
           public void receiveResultcivetGetVets(
                    com.cai.webservice.ExternalMessagesStub.CivetGetVetsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from civetGetVets operation
           */
            public void receiveErrorcivetGetVets(java.lang.Exception e) {
            }
                


    }
    