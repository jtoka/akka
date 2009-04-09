/**
 * Copyright (C) 2009 Scalable Solutions.
 */

package se.scalablesolutions.akka.kernel

import se.scalablesolutions.akka.collection._
import scala.collection.mutable.{HashMap}

trait Transactional {
  private[kernel] def begin
  private[kernel] def commit
  private[kernel] def rollback
}

sealed trait State[K, V] extends Transactional {
  def put(key: K, value: V)
  def remove(key: K)
  def get(key: K): V
  def contains(key: K): Boolean
  def elements: Iterator[(K, V)]
  def size: Int
  def clear
}

/**
 * Not thread-safe, but should only be using from within an Actor, e.g. one single thread at a time.
 */
sealed class TransientState[K, V] extends State[K, V] {
  private[kernel] var state = new HashTrie[K, V]
  private[kernel] var snapshot = state

  private[kernel] override def begin = {
    snapshot = state
  }

  private[kernel] override def commit = {
    snapshot = state
  }

  private[kernel] override def rollback = {
    state = snapshot
  }

  override def put(key: K, value: V) = {
    state = state.update(key, value)
  }

  override def remove(key: K) = {
    state = state - key
  }

  def get(key: K): V = state.get(key).getOrElse { throw new NoSuchElementException("No value for key [" + key + "]") }

  def contains(key: K): Boolean = state.contains(key)
 
  def elements: Iterator[(K, V)] = state.elements

  def size: Int = state.size

  def clear = state = new HashTrie[K, V]
}

final class TransientStringState extends TransientState[String, String]
final class TransientObjectState extends TransientState[String, AnyRef]

/**
 * Not thread-safe, but should only be using from within an Actor, e.g. one single thread at a time.
 */
trait UnitOfWork[K, V] extends State[K, V] with Transactional {
  this: TransientState[K, V] =>
  private[kernel] val changeSet = new HashMap[K, V]

  abstract override def begin = {
    super.begin
    changeSet.clear
  }

  abstract override def put(key: K, value: V) = {
    super.put(key, value)
    changeSet += key -> value
  }

  abstract override def remove(key: K) = {
    super.remove(key)
    changeSet -= key
  }
}

//class VectorState[T] {
//  private[kernel] var state: Vector[T] = EmptyVector
//  private[kernel] var snapshot = state
//  private[kernel] var unitOfWork: List[T] = Nil
//
//  private[kernel] def record = {
//    snapshot = state
//    unitOfWork = Nil
//  }
//
//  def add(elem: T): VectorState[T] = {
//    state = state + elem
//    unitOfWork ::= elem
//  }
//
//  def get(index: Int): T = state(index)
//
//  def size: Int = state.size
//}
//
//
