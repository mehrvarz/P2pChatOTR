P2pChatSMP
==========

P2pChatSMP is a peer-to-peer console chat application for clients located behind firewalls. It supports end-to-end encryption with Off-the-Record Messaging and Socialist Millionairs' Protocol, implemented using Java-OTR.


System requirements
-------------------

To build and run this project, the following 3rd party software packages needs to be installed: 

    Scala 2.9.x, OpenJDK 6, Ant

On Ubuntu 12.04, for example, you would install:

    apt-get install scala ant


Building from source
--------------------

To build the project, run:

    ./make

The `make` script will work in three steps:

  1. compile Java-OTR classes in src/ca/ using ant
  2. compile P2pChatSMP classes in src/*.scala using scalac
  3. create P2pChatSMP.jar by running ./makejar script using the JDK jar tool


Running P2pChatSMP
------------------

A `run` script is provided to save you from classpath issues. `run` uses the Scala runtime to execute the specified class. Two instances of P2pChatSMP need to run in parallel, so they can connect to each other and transfer data back and forth. Running both instances on the same machine is possible, but the purpose of this application is to bridge clients located behind firewals. Example setup: two PC's, one connected via DSL, one connected via 3G-card or via smartphone tethering. 

P2pChatSMP works just like P2pBase (see: https://github.com/mehrvarz/P2pCore), creating a direct p2p link between two clients. But instead of using pre-exchanged public RSA keys for end-to-end encryption, OTR messaging and SMP are being used.

    ./run timur.p2pChatSMP.P2pChatSMP alice.msn.com msn bob@msn.com
    ./run timur.p2pChatSMP.P2pChatSMP bob.msn.com msn alice@msn.com

As soon as the 2nd instance is started a direct P2P link will be established (if technically possible; otherwise a relayed connection will be used). OTR is started automatically. Quoting [Java-OTR](http://www.cypherpunks.ca/otr/) on the use of SMP:

    Type any messages (not starting with '/') in either console. The messages 
    will be automatically encrypted. Both the encrypted and the plaintext 
    messages will be displayed.

    If you want to start SMP, type "/is", or "/isq" if you have a suggested
    question for the other side. The commands to respond to and abort SMP
    are "/rs" and "/as" respectively.
 
    To end the private conversation, type "/disc" (disconnect).

To end the app hit Ctrl-C. Shown below is a P2pChatSMP log of one of the two instances (Alice):

    P2pChatSMP accountname=alice.msn.com protocol=msn recipient=bob@msn.com
    P2pChatSMP relaySocket.getLocalPort=-1 relayServer=109.74.203.226 relayPort=18771
    P2pChatSMP receiveHandler send encrypted initialMsg='...'
    P2pChatSMP combinedUdpAddrString this peer udpAddress=89.201.71.60:33790|192.168.1.135:33790
    P2pChatSMP receiveMsgHandler other peer combindedUdpAddress='89.201.71.60:55130|192.168.1.135:55130'
    P2pChatSMP datagramSendThread udpIpAddr='192.168.1.135' udpPortInt=33790 abort
    P2pChatSMP datagramSendThread udpIpAddr='89.201.71.60' udpPortInt=55130 connected
    (input:) Hello
    To OTR:5:Hello
    Injecting message to the recipient:29:Hello
    From network:326:?OTR:AAICAAAAxIvEqfcXB0I...
    Injecting message to the recipient:274:?OTR:AAIKAAAAwJWlDTaJP...
    From network:690:?OTR:AAIRAAAAENsQ4J2Rx8Nq...
    New fingerprint is created.
    Writing fingerprints.
    Updating context list.
    AKE succeeded
    Injecting message to the recipient:666:?OTR:AAISAAAB0tUvh3ZcWUOHl40...
    (input:) /is
    Please input the secret
    (input:) Berlin
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
    From OTR:0:
    (input:) Hi there...
    To OTR:11:Hi there...
    Injecting message to the recipient:390:?OTR:AAIDAAAAAAMAAAADAAAAwMyTRhBvrwoadr...

License
-------

Source code is licensed under the GNU General Public License, Version 3

See [LICENSE](blob/master/LICENSE)

Copyright (C) 2012 timur.mehrvarz@gmail.com

1st-party and 3rd-party code being used:

- P2pCore https://github.com/mehrvarz/P2pCore

- Java-OTR http://www.cypherpunks.ca/otr/


