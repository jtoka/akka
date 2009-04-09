/**
 * Copyright (C) 2009 Scalable Solutions.
 */

package se.scalablesolutions.akka.kernel

sealed abstract class TestMessage
case object Ping extends TestMessage
case object Pong extends TestMessage
case object OneWay extends TestMessage
case object Die extends TestMessage
case object NotifySupervisorExit extends TestMessage
