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
      println("arg1: p2p secret")
      println("arg2: smp secret")
      return
    }
    new P2pChatSMP(args(0),args(1)).start
  }
}

class P2pChatSMP(p2pSecret:String, smpSecret:String) extends P2pBase {
  val accountname="dummy"
  val protocol="dummy"
  val recipient="dummy"

  // Generate the keys
  val otrInterface = new UserState(new ca.uwaterloo.crysp.otr.crypt.jca.JCAProvider())
	val otrContext = otrInterface.getContext(accountname, protocol, recipient)
  val otrCallbacks = new LocalCallback(this, otrContext)
  
  matchSource = p2pSecret
  matchTarget = p2pSecret

  /** p2p connected now (if relayBasedP2pCommunication is set, p2p is relayed; else it is direct) */
  override def p2pSendThread() {

    if(publicUdpAddrString>otherUdpAddrString) {
      log("first msg: stand up")
      // client A will send msg to get to "AKE succeeded" state, where the other client will do initiateSmp()
      otrMsgSend("stand up")
    }

    def otrMsgSend(str:String) {
			println("\033[31mTo OTR:"+str.length+":\033[0m"+str)
			val tlvs = new Array[OTRTLV](1)
			tlvs(0) = new TLV(9, "TestTLV".getBytes)
			otrInterface.messageSending(accountname, protocol, recipient,
					str, tlvs, Policy.FRAGMENT_SEND_ALL, otrCallbacks)
			/*if(str.length()!=0){
				println("\033[31mTo network:"+str.length()+":\033[35m"+str+"\033[0m");
				otrContext.fragmentAndSend(str,  otrCallbacks);
			}*/
    }
    
  	val bufferedReader = new BufferedReader(new InputStreamReader(System.in))
		while(!p2pQuitFlag) {
			try {
				val str = bufferedReader.readLine
				if(str.startsWith("/isq")) {
					println("Please input the question")
					val questionString = bufferedReader.readLine
					println("Please input the secret")
					val secretString = bufferedReader.readLine
					otrContext.initiateSmp_q(questionString, secretString, otrCallbacks)

				} else if(str.startsWith("/is")) {
					println("Please input the secret")
					val secretString = bufferedReader.readLine
					otrContext.initiateSmp(secretString, otrCallbacks)

				} else if(str.startsWith("/rs")) {
					println("Please input the secret")
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
		println("\033[31mFrom network:"+str.length+":\033[35m"+str+"\033[0m")
		val stringTLV = otrInterface.messageReceiving(accountname, protocol, recipient, str, otrCallbacks)
		if(stringTLV!=null){
			val msg = stringTLV.msg
			println("\033[31mFrom OTR:"+msg.length+":\033[0m"+msg)
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
    		println("\033[31mInjecting message to the recipient:"+msg.length()+":\033[35m"+msg+"\033[0m")
	    	p2pBase.p2pSend(msg)
	    }
	  }

	  def getOtrPolicy(conn:OTRContext) :Int = {
		  return Policy.DEFAULT
	  }

	  def goneSecure(context:OTRContext) {
		  println("\033[31mAKE succeeded\033[0m")

      if(publicUdpAddrString<otherUdpAddrString) {
        log("goneSecure -> init OMP with smpSecret="+smpSecret)   // never log the secret
      	if(smpSecret!=null && smpSecret.length>0) {
      	  // on OTRL_SMPEVENT_ASK_FOR_SECRET the other client will do respondSmp()
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
		  println("\033[31mNew fingerprint is created.\033[0m")
	  }

	  def stillSecure(context:OTRContext, is_reply:Int) {
		  println("\033[31mStill secure.\033[0m")
	  }

	  def updateContextList() {
		  println("\033[31mUpdating context list.\033[0m")
	  }

	  def writeFingerprints() {
		  println("\033[31mWriting fingerprints.\033[0m")
	  }

	  def errorMessage(context:OTRContext, err_code:Int) :String = {

		  if(err_code==OTRCallbacks.OTRL_ERRCODE_MSG_NOT_IN_PRIVATE)
			  return "You sent an encrypted message, but we finished the private conversation."
		  return null
	  }

	  def handleMsgEvent(msg_event:Int, context:OTRContext, message:String) {

		  if(msg_event==OTRCallbacks.OTRL_MSGEVENT_CONNECTION_ENDED)
			  println("\033[31mThe private connection has already ended.\033[0m")

		  else if(msg_event==OTRCallbacks.OTRL_MSGEVENT_RCVDMSG_NOT_IN_PRIVATE)
			  println("\033[31mWe received an encrypted message, but we are not in encryption state.\033[0m")
	  }

	  def handleSmpEvent(smpEvent:Int, context:OTRContext, progress_percent:Int, question:String) {

		  if(smpEvent == OTRCallbacks.OTRL_SMPEVENT_ASK_FOR_SECRET) {
			  println("\033[31mThe other side has initialized SMP. Please respond with /rs.\033[0m")

        if(publicUdpAddrString>otherUdpAddrString) {
          log("handleSmpEvent respond OMP with smpSecret="+smpSecret)   // never log the secret
        	if(smpSecret!=null && smpSecret.length>0) {
        	  // if everything goes well, both clients will receive OTRL_SMPEVENT_SUCCESS
            otrContext.respondSmp(smpSecret, otrCallbacks)
          }
        }
	    }
		  else if(smpEvent == OTRCallbacks.OTRL_SMPEVENT_ASK_FOR_ANSWER) {
			  println("\033[31mThe other side has initialized SMP, with question:" +
			          question + ", Please respond with /rs.\033[0m")
      }
		  else if(smpEvent == OTRCallbacks.OTRL_SMPEVENT_SUCCESS) {
			  println("\033[31mSMP succeeded.\033[0m")
        log("************* SMP succeeded ***************")
			}
		  else if(smpEvent == OTRCallbacks.OTRL_SMPEVENT_FAILURE) {
			  println("\033[31mSMP failed.\033[0m")
			}

	  }
  }
}

