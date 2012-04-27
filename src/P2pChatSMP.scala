/*
 * This file is part of P2pChatSMP
 *
 * Copyright (C) 2012 Timur Mehrvarz, timur.mehrvarz(at)gmail.com
 *
 * Based in part on: Java OTR library
 *
 * Copyright (C) 2008-2009  Ian Goldberg, Muhaimeen Ashraf, Andrew Chung,
 *                          Can Tang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation <http://www.gnu.org/licenses/>, either 
 * version 3 of the License, or (at your option) any later version.
 */

package timur.p2pChatSMP

import java.io.{ BufferedReader, InputStreamReader }
import ca.uwaterloo.crysp.otr.{ UserState, TLV }
import ca.uwaterloo.crysp.otr.iface.{ OTRTLV, Policy }
import ca.uwaterloo.crysp.otr.iface.{ OTRContext, OTRInterface, OTRCallbacks }

import timur.p2pCore._

object P2pChatSMP {
  def main(args:Array[String]): Unit = {
    if(args.length<2) {
      println("arg1: p2pSecret")
      println("arg2: smpSecret")
      return
    }
    new P2pChatSMP(args(0),args(1), null).start
  }
}

class P2pChatSMP(p2pSecret:String, smpSecret:String, parent:timur.p2pChatSMP.LogClassTrait) extends P2pBase {
  val accountname="dummy"
  val protocol="dummy"
  val recipient="dummy"

  // Generate the keys
  val otrInterface = new UserState(new ca.uwaterloo.crysp.otr.crypt.jca.JCAProvider())
	val otrContext = otrInterface.getContext(accountname, protocol, recipient)
  val otrCallbacks = new LocalCallback(this, otrContext)
  
  matchSource = p2pSecret
  matchTarget = p2pSecret

  override def log(str:String) {
    if(parent!=null) 
      parent.log(str)
    else
      super.log(str)
  }

/*
  // use this method only if running from within a runnable jar
  override def initHostPubKey() {
  	val relayKeyPathInRunnableJar = "/relaykey.pub"
  	//log("initHostPubKey read relayKeyPathInRunnableJar="+relayKeyPathInRunnableJar)
    val is = getClass.getResourceAsStream(relayKeyPathInRunnableJar)
  	if(is==null) {
    	log("initHostPubKey failed to read relayKeyPathInRunnableJar="+relayKeyPathInRunnableJar)
      hostPubKey = io.Source.fromFile("relaykey.pub").mkString
      if(hostPubKey.length>0)
      	log("initHostPubKey from filesystem="+hostPubKey.substring(0,math.min(60,hostPubKey.length)))
  	} else {
      hostPubKey = io.Source.fromInputStream(is).mkString
      if(hostPubKey.length>0)
        log("initHostPubKey from runnableJar="+hostPubKey.substring(0,math.min(60,hostPubKey.length)))
    }
    if(hostPubKey.length<=0)
      log("initHostPubKey failed to read keyFile")
  }
*/

//val esc1 = "\033[31m"
//val esc2 = "\033[0m"
//val esc3 = "\033[35m"

//val esc1 = "{"
//val esc2 = "} "
//val esc3 = "=="

  val esc1 = "  "
  val esc2 = ""
  val esc3 = ""

  /** p2p connected now (if relayBasedP2pCommunication is set, p2p is relayed; else it is direct) */
  override def p2pSendThread() {

    if(publicUdpAddrString>otherUdpAddrString) {
      val firstMessage = "stand up"
      log("send first msg: '"+firstMessage+"'")
      // client A will send msg to get to "AKE succeeded" state, where the other client will do initiateSmp()
      otrMsgSend(firstMessage)
    }

    def otrMsgSend(str:String) {
			//log(esc1+"To OTR:"+str.length+":"+esc2+str)
			if(str!=null && str.length>0) {
			  log("> "+str)
			  val tlvs = new Array[OTRTLV](1)
			  tlvs(0) = new TLV(9, "TestTLV".getBytes)
			  otrInterface.messageSending(accountname, protocol, recipient,
					  str, tlvs, Policy.FRAGMENT_SEND_ALL, otrCallbacks)
			  /*if(str.length()!=0){
				  log(esc1+"To network:"+str.length()+":"+esc3+str+esc2);
				  otrContext.fragmentAndSend(str,  otrCallbacks);
			  }*/
			}
    }
    
  	val bufferedReader = new BufferedReader(new InputStreamReader(System.in))
		while(!p2pQuitFlag) {
			try {
				val str = bufferedReader.readLine
				if(str.startsWith("/isq")) {
					log("Please input the question")
					val questionString = bufferedReader.readLine
					log("Please input the secret")
					val secretString = bufferedReader.readLine
					otrContext.initiateSmp_q(questionString, secretString, otrCallbacks)

				} else if(str.startsWith("/is")) {
					log("Please input the secret")
					val secretString = bufferedReader.readLine
					otrContext.initiateSmp(secretString, otrCallbacks)

				} else if(str.startsWith("/rs")) {
					log("Please input the secret")
					val secretString = bufferedReader.readLine
					otrContext.respondSmp(secretString, otrCallbacks)

				} else if(str.startsWith("/as")) {
					otrContext.abortSmp(otrCallbacks)

				} else if(str.startsWith("/disc")) {
					otrContext.disconnect(otrCallbacks)

				} else {
				  otrMsgSend(str)
				}

			} catch {
			  case ex:Exception =>
  				ex.printStackTrace
			}
		}

    log("p2pSendThread p2pQuitFlag="+p2pQuitFlag)
    p2pQuit(true)
  }

  /** received data string from the remote client per UDP (or via relay server as a fallback) */
  override def p2pReceiveHandler(str:String, host:String, port:Int) {
		log(esc1+"From network:"+str.length+":"+esc3+str.substring(0,math.min(str.length,60))+esc2)
		val stringTLV = otrInterface.messageReceiving(accountname, protocol, recipient, str, otrCallbacks)
		if(stringTLV!=null){
			val msg = stringTLV.msg
			if(msg.length>0) {
			  //log(esc1+"From OTR:"+msg.length+":"+esc2+msg)
			  log("< "+msg)
			}
		}
  }

  /** bring the relay connection down */
  override def relayQuit() {
    // we also want to bring p2p down
    // todo: this is not wise, if we bring down relay link while we are still using p2p link
    log("relayQuit -> p2pQuitFlag=true")
    p2pQuitFlag=true
    super.relayQuit
  }

/*
  // todo: test this with relay fallback again
  override def relayReceiveHandler(str:String) {
    // we receive data via (or from) the relay server 
    // in p2p mode, this is not being used: all data goes to p2pReceiveHandler (even if relayed as a fallback)
    log("relayReceiveHandler str='"+str+"' UNEXPECTED IN P2P MODE ###########")
  }
*/

  class LocalCallback(p2pBase:P2pBase, otrContext:OTRContext) extends OTRCallbacks {

	  def injectMessage(accName:String, prot:String, rec:String, msg:String) {
		  if(msg!=null) {
    		log(esc1+"Injecting message to the recipient:"+msg.length+":"+esc3+msg.substring(0,math.min(msg.length,60))+esc2)
	    	p2pBase.p2pSend(msg)
	    }
	  }

	  def getOtrPolicy(conn:OTRContext) :Int = {
		  return Policy.DEFAULT
	  }

	  def goneSecure(context:OTRContext) {
		  log(esc1+"AKE succeeded"+esc2)

      if(publicUdpAddrString<otherUdpAddrString) {
        log("goneSecure -> init OMP with smpSecret="+smpSecret)   // never log the secret
      	if(smpSecret!=null && smpSecret.length>0) {
      	  // Client B will initiateSmp; on OTRL_SMPEVENT_ASK_FOR_SECRET client A will do respondSmp()
          otrContext.initiateSmp(smpSecret, otrCallbacks)
        }
      }
	  }

	  def isLoggedIn(accountname:String, protocol:String, recipient:String) :Int = {
		  return 1
	  }

	  def maxMessageSize(context:OTRContext) :Int = {
		  return 1000
	  }

	  def newFingerprint(us:OTRInterface, accountname:String , protocol:String, 
	                     username:String, fingerprint:Array[Byte]) {
		  log(esc1+"New fingerprint is created."+esc2)
	  }

	  def stillSecure(context:OTRContext, is_reply:Int) {
		  log(esc1+"Still secure."+esc2)
	  }

	  def updateContextList() {
		  log(esc1+"Updating context list."+esc2)
	  }

	  def writeFingerprints() {
		  log(esc1+"Writing fingerprints."+esc2)
	  }

	  def errorMessage(context:OTRContext, err_code:Int) :String = {

		  if(err_code==OTRCallbacks.OTRL_ERRCODE_MSG_NOT_IN_PRIVATE)
			  return "You sent an encrypted message, but we finished the private conversation."
		  return null
	  }

	  def handleMsgEvent(msg_event:Int, context:OTRContext, message:String) {

		  if(msg_event==OTRCallbacks.OTRL_MSGEVENT_CONNECTION_ENDED)
			  log(esc1+"The private connection has already ended."+esc2)

		  else if(msg_event==OTRCallbacks.OTRL_MSGEVENT_RCVDMSG_NOT_IN_PRIVATE)
			  log(esc1+"We received an encrypted message, but we are not in encryption state."+esc2)
	  }

	  def handleSmpEvent(smpEvent:Int, context:OTRContext, progress_percent:Int, question:String) {

		  if(smpEvent == OTRCallbacks.OTRL_SMPEVENT_ASK_FOR_SECRET) {
        if(publicUdpAddrString>otherUdpAddrString) {
          log("handleSmpEvent respond OMP with smpSecret="+smpSecret)   // never log the secret?
        	if(smpSecret!=null && smpSecret.length>0) {
        	  // Client A will respondSmp; if all goes well, both clients will receive OTRL_SMPEVENT_SUCCESS
            otrContext.respondSmp(smpSecret, otrCallbacks)
          }
        } else {
  			  log(esc1+"The other side has initialized SMP. Please respond with /rs."+esc2)
  			}
	    }
		  else if(smpEvent == OTRCallbacks.OTRL_SMPEVENT_ASK_FOR_ANSWER) {
			  log(esc1+"The other side has initialized SMP, with question:" +
			          question + ", Please respond with /rs."+esc2)
      }
		  else if(smpEvent == OTRCallbacks.OTRL_SMPEVENT_SUCCESS) {
			  //log(esc1+"SMP succeeded."+esc2)
        log("************* SMP succeeded ***************")
			}
		  else if(smpEvent == OTRCallbacks.OTRL_SMPEVENT_FAILURE) {
			  //log(esc1+"SMP failed."+esc2)
        log("************* SMP failed ***************")
			}

	  }
  }
}

