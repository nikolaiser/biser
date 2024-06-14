package com.nikolaiser.biser.distage

import besom.internal.Context
import besom.internal.Output
import besom.internal.Result
import besom.zio.toFutureZIOTask
import izumi.functional.bio.Exit.Trace
import izumi.functional.quasi.QuasiAsync
import izumi.functional.quasi.QuasiIO
import izumi.functional.quasi.QuasiIORunner
import izumi.functional.quasi.QuasiTemporal
import zio.ZIO
import zio.interop.catz.asyncInstance

import scala.concurrent.duration.FiniteDuration
import scala.jdk.DurationConverters.*

given (using Context): QuasiIO[Output] with
  def flatMap[A, B](fa: Output[A])(f: A => Output[B]): Output[B] =
    fa.flatMap(f)

  def definitelyRecoverUnsafeIgnoreTrace[A](action: => Output[A])(
      recover: Throwable => Output[A]
  ): Output[A] = action.recoverWith(recover)

  def map[A, B](fa: Output[A])(f: A => B): Output[B] = fa.map(f)

  def map2[A, B, C](fa: Output[A], fb: => Output[B])(
      f: (A, B) => C
  ): Output[C] = fa.zip(fb).map(f.tupled)

  def maybeSuspendEither[A](
      eff: => Either[Throwable, A]
  ): Output[A] =
    besom.internal.Output.apply(Result.evalEither(eff))

  def redeem[A, B](
      action: => Output[A]
  )(failure: Throwable => Output[B], success: A => Output[B]): Output[B] =
    action.flatMap(success).recoverWith(failure)

  def bracket[A, B](acquire: => Output[A])(
      release: A => Output[Unit]
  )(use: A => Output[B]): Output[B] =
    acquire.flatMap { a =>
      use(a).flatMap(r => release(a).map(_ => r))
    }

  def bracketCase[A, B](acquire: => Output[A])(
      release: (A, Option[Throwable]) => Output[Unit]
  )(use: A => Output[B]): Output[B] =
    acquire.flatMap { a =>
      use(a)
        .flatMap { r =>
          release(a, None).map(_ => r)
        }
        .tapError(err => release(a, Some(err)))
    }

  def definitelyRecoverWithTrace[A](action: => Output[A])(
      recoverWithTrace: (Throwable, Trace[Throwable]) => Output[A]
  ): Output[A] = definitelyRecoverUnsafeIgnoreTrace(action)(e => recoverWithTrace(e, Trace.ThrowableTrace(e)))

  def maybeSuspend[A](eff: => A): Output[A] =
    Output.apply(Result.defer(eff))

  def pure[A](a: A): Output[A] = Output(a)

  def fail[A](t: => Throwable): Output[A] = Output.fail(t)

given (using Context): QuasiAsync[Output] with
  def async[A](
      effect: (Either[Throwable, A] => Unit) => Unit
  ): Output[A] = Output.eval(asyncInstance[Any].async_(effect))

  def parTraverse[A, B](l: IterableOnce[A])(
      f: A => Output[B]
  ): Output[List[B]] = Output.parSequence(l.iterator.map(f).toList)

  def parTraverse_[A](l: IterableOnce[A])(
      f: A => Output[Unit]
  ): Output[Unit] = parTraverse(l)(f).map(_ => ())

  def parTraverseN[A, B](n: Int)(l: IterableOnce[A])(
      f: A => Output[B]
  ): Output[List[B]] = ???

  def parTraverseN_[A](n: Int)(l: IterableOnce[A])(
      f: A => Output[Unit]
  ): Output[Unit] = ???

given (using Context): QuasiTemporal[Output] with
  def sleep(duration: FiniteDuration): Output[Unit] =
    Output.eval(ZIO.sleep(duration.toJava))

given (using Context): QuasiIORunner[Output] with
  def run[A](f: => Output[A]): A = ???
