P2pChatSMP
==========

P2pChatSMP is a peer-to-peer chat application for clients located behind firewalls. P2pChatSMP uses [Off-the-Record Messaging](http://de.wikipedia.org/wiki/Off-the-Record_Messaging) and [Socialist Millionaire' Protocol](http://en.wikipedia.org/wiki/Socialist_millionaire) to implement end-to-end encryption, so your conversations are secure and private. No host account (XMPP or other) is needed to use P2pChatSMP. So also your usage pattern is not trackable.


System requirements
-------------------

To build or run P2pChatSMP, the following 3rd party software must be installed:

    OpenJDK 6, Scala 2.9.x, Ant

On Ubuntu 12.04 you would:

    apt-get install scala ant


Running P2pChatSMP
------------------

The code repository contains the required binaries. A `run` script is provided for convenience. `run` uses the Scala runtime to execute the named class. Two P2pChatSMP instances need to be executed in parallel, so they can connect to each other. Running both instances on the same machine is possible, but the purpose of this application is to bridge clients located behind discrete firewalls. Possible setups: two machines in completely separate locations. Or: two PC's, one connected via DSL or cable, the other one connected via mobile internet.

P2pChatSMP works similar to P2pBase (see: https://github.com/mehrvarz/P2pCore). Instead of using pre-exchanged public keys for end-to-end encryption, however, OTR messaging and SMP are being used. P2pChatSMP needs two secret strings on start:

    (Alice) ./run timur.p2pChatSMP.P2pChatSMP paris texas
    (Bob)   ./run timur.p2pChatSMP.P2pChatSMP paris texas

The first string argument ("paris") will be used to match the two clients. The second string argument ("texas") will be used as the OTR/SMP secret. Both clients must use the same secret strings.

As soon as a p2p connection has been established, OTR and SMP will be started automatically. Five to ten seconds later, SMP should be complete, so that a secure and private conversation can take place. To end the application hit Ctrl-C. 

Shown below is a P2pChatSMP log of Alice's client instance:

    P2pChatSMP accountname=alice.msn.com protocol=msn recipient=bob@msn.com
    P2pChatSMP relaySocket.getLocalPort=-1 relayServer=109.74.203.226 relayPort=18771
    P2pChatSMP receiveHandler send encrypted initialMsg='...'
    P2pChatSMP combinedUdpAddrString this peer udpAddress=89.201.71.60:33790|192.168.1.135:33790
    P2pChatSMP receiveMsgHandler other peer combindedUdpAddress='89.201.71.60:55130|192.168.1.135:55130'
    P2pChatSMP datagramSendThread udpIpAddr='192.168.1.135' udpPortInt=33790 abort
    P2pChatSMP datagramSendThread udpIpAddr='89.201.71.60' udpPortInt=55130 connected
    From network:32:stand up
    Injecting message to the recipient:326:?OTR:AAICAAAAxPaF08CY3FVioRfrGCgEvJ...
    From OTR:8:stand up
    From network:274:?OTR:AAIKAAAAwNvZMcndXAqJDvdqd/p9aWtEHKKyN...
    From network:690:?OTR:AAIRAAAAENsQ4J2Rx8Nq...
    New fingerprint is created.
    Writing fingerprints.
    Updating context list.
    AKE succeeded
    P2pChatSMP goneSecure -> init OMP with smpSecret=berlin
    Injecting message to the recipient:666:?OTR:AAISAAAB0tUvh3ZcWUOHl40...
    Injecting message to the recipient:991:?OTR,1,2,?OTR:AAIDAQAAAAEAAAABAAAAwDA...
    Injecting message to the recipient:515:?OTR,2,2,4C7tqiIkJV//ZdC8jimctJHJhd7...
    From network:991:?OTR,1,3,?OTR:AAIDAQAAAAEAAAACAAAAw...
    From network:991:?OTR,2,3,fNejafJRYsoWj12DJKOEIrXgYH7qVwQHZXDZ...
    From network:626:?OTR,3,3,TOgcciO69O/o6hFtr6nnCwSdLQJGimoi+ekQ...
    Injecting message to the recipient:991:?OTR,1,3,?OTR:AAIDAQAAAAIAAAACAAAAwCEOW...
    Injecting message to the recipient:991:?OTR,2,3,9oRam+5af6ZqgvkJ4UGuwYKX8ulPaJ...
    Injecting message to the recipient:58:?OTR,3,3,1YciE3FSKuYn41KCGSHSYxT7LscjNh...
    From OTR:0:
    From network:942:?OTR:AAIDAQAAAAIAAAADAAAA...
    Writing fingerprints.
    SMP succeeded.
    P2pChatSMP ************* SMP succeeded ***************


Building from source
--------------------

To build the project, run:

    ./make

`make` script will work three steps:

1. compile Java-OTR classes located in src/ca/ using ant and javac
2. compile P2pChatSMP classes located in src/ using scalac
3. create P2pChatSMP.jar by running ./makejar script


Licenses
--------

- P2pChatSMP, P2pCore source code and library

  licensed under the GNU General Public [LICENSE](P2pChatSMP/blob/master/licenses/LICENSE), Version 3.

  Copyright (C) 2012 timur.mehrvarz@gmail.com

  https://github.com/mehrvarz/P2pChatSMP

  https://github.com/mehrvarz/P2pCore

- The Java Off-the-Record Messaging library

  covered by the LGPL [LICENSE](P2pChatSMP/blob/master/licenses/java-otr/COPYING).

  [README](P2pChatSMP/blob/master/licenses/java-otr/README)

  http://www.cypherpunks.ca/otr/
  
- Bouncy Castle 

  http://bouncycastle.org/

- Google Protobuf 

  https://code.google.com/p/protobuf/

- Apache Commons-codec 

  http://commons.apache.org/codec/


