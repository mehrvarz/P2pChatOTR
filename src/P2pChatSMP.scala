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

import timur.p2pCore._

object P2pChatSMP {
  def main(args:Array[String]): Unit = {
    if(args.length<3) {
      println("arg1: accountname (alice.msn.com)")
      println("arg2: protocol (msn)")
      println("arg3: recipient (bob@msn.com)")
      return
    }
    new P2pChatSMP(args(0), args(1), args(2)).start
  }
}

class P2pChatSMP(accountname:String, protocol:String, recipient:String) extends P2pBase {

  println("P2pChatSMP accountname="+accountname+" protocol="+protocol+" recipient="+recipient)
  // Generate the keys
  val otrInterface = new UserState(new ca.uwaterloo.crysp.otr.crypt.jca.JCAProvider())
  val otrCallbacks = new LocalCallback(this)
  
  /** p2p connected now (if relayBasedP2pCommunication is set, p2p is relayed; else it is direct) */
  override def p2pSendThread() {

  	val otrContext = otrInterface.getContext(accountname, protocol, recipient)
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
  override def relayReceiveHandler(str:String) {
    // we receive data via (or from) the relay server 
    // in p2p mode, this is not being used: all data goes to p2pReceiveHandler (even if relayed as a fallback)
    log("relayReceiveHandler str='"+str+"' UNEXPECTED IN P2P MODE ###########")
  }
*/
}

