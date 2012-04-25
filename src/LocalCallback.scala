/*
 * This file is part of P2pChatSMP
 *
 * Copyright (C) 2012 Timur Mehrvarz, timur.mehrvarz(at)gmail.com
 *
 * Based on: Java OTR library
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

import ca.uwaterloo.crysp.otr.iface.{ OTRContext, OTRInterface, OTRCallbacks, Policy }
import timur.p2pCore.P2pBase

class LocalCallback(p2pBase:P2pBase) extends OTRCallbacks {

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

		if(smpEvent == OTRCallbacks.OTRL_SMPEVENT_ASK_FOR_SECRET)
			println("\033[31mThe other side has initialized SMP. Pease respond with /rs.\033[0m")

		else if(smpEvent == OTRCallbacks.OTRL_SMPEVENT_ASK_FOR_ANSWER)
			println("\033[31mThe other side has initialized SMP, with question:" +
			        question + ", Please respond with /rs.\033[0m")

		else if(smpEvent == OTRCallbacks.OTRL_SMPEVENT_SUCCESS)
			println("\033[31mSMP succeeded.\033[0m")

		else if(smpEvent == OTRCallbacks.OTRL_SMPEVENT_FAILURE)
			println("\033[31mSMP failed.\033[0m")

	}
}

