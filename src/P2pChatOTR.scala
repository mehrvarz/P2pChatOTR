/*
 * This file is part of P2pChatOTR
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

package timur.p2pChat

import java.security.{ Security, MessageDigest }
import java.io.{ BufferedReader, InputStreamReader }
import ca.uwaterloo.crysp.otr.{ UserState, TLV }
import ca.uwaterloo.crysp.otr.iface.{ OTRTLV, Policy }
import ca.uwaterloo.crysp.otr.iface.{ OTRContext, OTRInterface, OTRCallbacks }

import timur.p2pCore._

object P2pChatOTR {
  def main(args:Array[String]): Unit = {
    if(args.length<1) {
      println("arg1: p2pSecret")
      println("arg2: smpSecret (optional)")
      return
    }
    if(args.length<2)
      new P2pChatOTR(args(0),null, null).start
    else
      new P2pChatOTR(args(0),args(1), null).start
  }
}

class P2pChatOTR(p2pSecret:String, smpSecret:String, parent:timur.p2pChat.LogClassTrait) extends P2pBase {
  val accountname = "dummy"
  val protocol = "dummy"
  val recipient = "dummy"

  val otrInterface = new UserState(new ca.uwaterloo.crysp.otr.crypt.jca.JCAProvider())
	val otrContext = otrInterface.getContext(accountname, protocol, recipient)
  var otrCallbacks = new LocalCallback(this, otrContext)
  
  matchSource = p2pSecret
  matchTarget = p2pSecret

  override def start() :Int = {
    init
/*
    var ret = readkeys
    if(ret!=0)
      return ret
*/
    return super.start
  }

  /**
   * prepare org.bouncycastle.crypto.encodings.PKCS1Encoding in RsaEncrypt/RsaDecrypt
   */
  def init() {
    Security.addProvider(new ext.org.bouncycastle.jce.provider.BouncyCastleProvider())
  }

  override def log(str:String) {
    if(parent!=null) 
      parent.log(str)
    else
      super.log(str)
  }

/*
  def readkeys() :Int = {
    // create folder for remote keys
    new java.io.File(keyFolderPath).mkdir
    val fullLocalKeyName = keyFolderPath+"/key.pub"

    try {
      // load local key pair
      log("readkeys fullLocalKeyName="+fullLocalKeyName+" used for fingerprint matching")
      pubKeyLocal = io.Source.fromFile(fullLocalKeyName).mkString
      log("readkeys read private key from private folder")
      privKeyLocal = io.Source.fromInputStream(openFileInput("key")).mkString

    } catch {
      case ex:Exception =>
        // generate local key pair
        log("readkeys generating local key pair")
        val keyPair = RsaKeyGenerate.rsaKeyGenerate
        privKeyLocal = Base64.encode(keyPair.getPrivate.getEncoded)
        pubKeyLocal = Base64.encode(keyPair.getPublic.getEncoded)

        // delete old private key
        val privateKeyFile = getFileStreamPath("key")
        if(privateKeyFile!=null) {
          log("readkeys privateKeyFile="+privateKeyFile.getAbsolutePath)
          if(privateKeyFile.exists) {
            log("readkeys privateKeyFile delete")
            privateKeyFile.delete
          }
        }

        // write new private key in private directory
        AndrTools.runOnUiThread(activity) { () =>
          Toast.makeText(activity, "creating new private and public keys", Toast.LENGTH_LONG).show
        }
        log("readkeys write new private key")
        // todo: send a toast in addition
        val fileOutputStream2 = openFileOutput("key", Context.MODE_PRIVATE)
        fileOutputStream2.write(privKeyLocal.getBytes)
        fileOutputStream2.close

        // write new public key in public keyFolderPath
        log("readkeys write new public key; len="+pubKeyLocal.length)
        Tools.writeToFile(keyFolderPath+"/key.pub", pubKeyLocal)
    }

    if(buildMatchStrings!=0)
      return -2
      
    return 0
  }
*/

//val esc1 = "\033[31m"
//val esc2 = "\033[0m"
//val esc3 = "\033[35m"

  val esc1 = ""
  val esc2 = ""
  val esc3 = ""

  def otrMsgSend(str:String) {
		//log(esc1+"To OTR:"+str.length+":"+esc2+str)
		if(str!=null && str.length>0) {
		  log("> "+str)
		  val tlvs = new Array[OTRTLV](1)
		  tlvs(0) = new TLV(9, "TestTLV".getBytes)
		  otrInterface.messageSending(accountname, protocol, recipient, str, tlvs, Policy.FRAGMENT_SEND_ALL, otrCallbacks)
		  /*if(str.length()!=0){
			  log(esc1+"To network:"+str.length()+":"+esc3+str+esc2);
			  otrContext.fragmentAndSend(str,  otrCallbacks);
		  }*/
		}
  } 

  /** p2p connected now (if relayBasedP2pCommunication is set, p2p is relayed; else it is direct) */
  override def p2pSendThread() {

    if(publicUdpAddrString>otherUdpAddrString) {
      val firstMessage = "start otr/smp..."
      //log("send first msg: '"+firstMessage+"'")
      // client A will send a msg to get to "AKE succeeded" state, where the other client will do initiateSmp()
      otrMsgSend(firstMessage)
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
					// todo: p2pQuit(true) ???

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

    // disconnect our relay connection (stay connected via direct p2p)
    if(relaySocket!=null && !relayBasedP2pCommunication) {
      log("disconnect relay connection: relaySocket.close")
      relayQuitFlag=true
      relaySocket.close
      relaySocket=null
    }

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

  /** received data string via relay server */
/*
  // in p2p mode, this is not being used: all data goes to p2pReceiveHandler (even if relayed as a fallback)
  // todo: test this with relay fallback again
  override def relayReceiveHandler(str:String) {
    log("relayReceiveHandler str='"+str+"' UNEXPECTED IN P2P MODE ###########")
  }
*/

/*
  def p2pReceiveMultiplexHandler(protoMultiplex:P2pCore.Message) {
    val command = protoMultiplex.getCommand
    if(command=="string") {
      val len = protoMultiplex.getMsgLength.asInstanceOf[Int]
      val receivedString = protoMultiplex.getMsgString
      //val id = protoMultiplex.getMsgId
      p2pReceivePreHandler(receivedString:String)
    }
  }
*/

  /**
   * p2pReceivePreHandler is called as soon as p2p data was encrypted
   * will process special commands, such as "//requestPubKeyFingerprint", "//pubKeyFingerprint=...", "//check", "//ack", "//quit"
   */
  override def p2pReceivePreHandler(str:String) {
    //log("p2pReceivePreHandler P2pChatOTR reset msgMsRcv; receiving=["+str+"]")
    if(str=="//requestPubKeyFingerprint") {
/*
      log("p2pReceivePreHandler: sending fingerprint of our pubkey on request="+pubKeyLocalFingerprint)
      // todo: don't have pubKeyLocalFingerprint yet
      p2pSend("//pubKeyFingerprint="+pubKeyLocalFingerprint)
*/
    } else if(str.startsWith("//pubKeyFingerprint=")) {
      val remoteKeyFingerprint = str.substring(19)
      log("p2pReceivePreHandler: remoteKeyFingerprint="+remoteKeyFingerprint)

/*
      // todo: don't have keyFolderPath yet; see readkeys()
      // todo: pubKeyRemote not a class variable yet
      // search all stored pub keys for a match to remoteKeyFingerprint
      pubKeyRemote = null
      val fileArray = new java.io.File(keyFolderPath).listFiles
      for(file <- fileArray.iterator.toList) {
        if(pubKeyRemote==null) {
          val fileName = file.getName.trim
          if(fileName.length>4 && fileName.endsWith(".pub") && fileName!="key.pub") {
            val key = io.Source.fromFile(keyFolderPath+"/"+fileName).mkString
            val messageDigest = MessageDigest.getInstance("SHA-1")
            messageDigest.update(Base64.decode(key))
            val fingerprint = RsaEncrypt.getHexString(messageDigest.digest)
            if(fingerprint==remoteKeyFingerprint) {
              log("p2pReceivePreHandler: found stored pubKeyRemote in file "+fileName)
              pubKeyRemote = key
            }
          }
        }
      }

      if(pubKeyRemote==null) {
        // todo: not found pubKeyRemote for remoteKeyFingerprint; request full key delivery
        p2pSend("//requestPubKey")
        return
      }
      
      // pubKeyRemote for remoteKeyFingerprint found in local folder; nothing more to do

    } else if(str=="//requestPubKey") {
      // todo: send full public key
      // p2pSend("//pubKey="+...)

    } else if(str.startsWith("//pubKey=")) {
      val remoteKey = str.substring(8)
      log("p2pReceivePreHandler: remoteKey="+remoteKey)
      // todo: store remoteKey
*/

    } else {
      super.p2pReceivePreHandler(str) // -> p2pReceiveHandler()
    }
  }

  class LocalCallback(p2pBase:P2pBase, otrContext:OTRContext) extends OTRCallbacks {

	  def injectMessage(accName:String, prot:String, rec:String, msg:String) {
		  if(msg!=null) {
    		log(esc1+"Injecting message to the recipient:"+msg.length+":"+esc3+msg.substring(0,math.min(msg.length,60))+esc2)
	    	p2pBase.p2pSend(msg)  // ,"rsastr" 
	    	                      // if we send anything other than default "string", we must implement p2pReceiveMultiplexHandler
	    }
	  }

	  def getOtrPolicy(conn:OTRContext) :Int = {
		  return Policy.DEFAULT
	  }

	  def goneSecure(context:OTRContext) {
		  log(esc1+"AKE succeeded"+esc2)

      if(publicUdpAddrString<otherUdpAddrString) {
    	  // Client B will initiateSmp; on OTRL_SMPEVENT_ASK_FOR_SECRET client A will do respondSmp()
      	if(smpSecret!=null && smpSecret.length>0) {
          // log("goneSecure -> init OMP with smpSecret="+smpSecret)   // never log the secret
          otrContext.initiateSmp(smpSecret, otrCallbacks)
        } else {
          // log("goneSecure -> init OMP with p2pSecret="+p2pSecret)   // never log the secret
          otrContext.initiateSmp(p2pSecret, otrCallbacks)
        }
      }
	  }

	  def isLoggedIn(accountname:String, protocol:String, recipient:String) :Int = {
		  return 1
	  }

	  def maxMessageSize(context:OTRContext) :Int = {
	    // todo: verify value
		  return 1000
	  }

	  def newFingerprint(us:OTRInterface, accountname:String , protocol:String, 
	                     username:String, fingerprint:Array[Byte]) {
		  log(esc1+"New fingerprint is created."+esc2)
	    // todo: show fingerprint
	  }

	  def stillSecure(context:OTRContext, is_reply:Int) {
		  log(esc1+"Still secure."+esc2)
	  }

	  def updateContextList() {
		  log(esc1+"Updating context list."+esc2)
	  }

	  def writeFingerprints() {
		  log(esc1+"Writing fingerprints."+esc2)
		  // ???
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
      	  // Client A will respondSmp; if all goes well, both clients will receive OTRL_SMPEVENT_SUCCESS
        	if(smpSecret!=null && smpSecret.length>0) {
            // log("handleSmpEvent respond OMP with smpSecret="+smpSecret)   // never log the secret
            otrContext.respondSmp(smpSecret, otrCallbacks)
          } else {
            // log("handleSmpEvent respond OMP with p2pSecret="+p2pSecret)   // never log the secret
            otrContext.respondSmp(p2pSecret, otrCallbacks)
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
        p2pEncryptedCommunication
			}

		  else if(smpEvent == OTRCallbacks.OTRL_SMPEVENT_FAILURE) {
			  //log(esc1+"SMP failed."+esc2)
        log("************* SMP failed ***************")
        // abort p2p
        p2pQuit(true)
			}

	  }
  }

  /** otr encryption now in place */
  def p2pEncryptedCommunication() {
    // start pubkey exchange
    //p2pSend("//requestPubKeyFingerprint")
    p2pWatchdog
  }
}

