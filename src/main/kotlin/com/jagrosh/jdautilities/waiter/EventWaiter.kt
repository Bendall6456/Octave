/*
 * MIT License
 *
 * Copyright (c) 2020 Melms Media LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.jagrosh.jdautilities.waiter

import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.EventListener
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

/**
 * A simple object used primarily for entities found in [com.jagrosh.jdautilities.menu].
 *
 * <p>The EventWaiter is capable of handling specialized forms of [GenericEvent]
 * that must meet criteria not normally specifiable without implementation of an [EventListener].
 *
 * <p>If you intend to use the EventWaiter, it is highly recommended you <b>DO NOT create multiple EventWaiters</b>!
 * Doing this will cause unnecessary increases in memory usage.
 *
 * @author John Grosh (jagrosh)
 * @author Avarel
 */
class EventWaiter : EventListener {
    private val waiters = mutableMapOf<Class<*>, MutableList<Waiter<GenericEvent>>>()
    private val scheduledExecutor = Executors.newSingleThreadScheduledExecutor()

    @Suppress("UNCHECKED_CAST")
    fun <T : GenericEvent> waitForEvent(cls: Class<in T>,
                                        predicate: (T) -> Boolean,
                                        action: (T) -> Unit,
                                        timeout: Long,
                                        unit: TimeUnit?,
                                        timeoutAction: (() -> Unit)?): Waiter<T> {
        val list = waiters.getOrPut(cls, ::mutableListOf)

        val waiter = Waiter(cls, predicate, action)
        list.add(waiter as Waiter<GenericEvent>)

        if (timeout > 0) {
            requireNotNull(unit)

            scheduledExecutor.schedule({
                if (list.remove(waiter)) {
                    timeoutAction?.invoke()
                }
            }, timeout, unit)
        }

        return waiter
    }

    @Suppress("UNCHECKED_CAST")
    override fun onEvent(event: GenericEvent) {
        var cls: Class<in GenericEvent> = event.javaClass

        while (cls.superclass != null) {
            waiters[cls]?.removeIf {
                @Suppress("SENSELESS_COMPARISON")
                it == null || it.attempt(event)
            }
            cls = cls.superclass
        }
    }

    fun <T : GenericEvent> waitFor(cls: Class<T>, action: (T) -> Unit): WaiterBuilder<T> = WaiterBuilder(cls, action)

    fun <T : GenericEvent> waitFor(cls: Class<T>, action: Consumer<T>): WaiterBuilder<T> = WaiterBuilder(cls) { action.accept(it) }

    // builder
    inner class WaiterBuilder<T : GenericEvent>(private var cls: Class<T>, private var action: (T) -> Unit) {
        private var predicate: ((T) -> Boolean) = { true }

        fun predicate(predicate: (event: T) -> Boolean): WaiterBuilder<T> {
            this.predicate = predicate
            return this
        }

        fun noTimeout(): Waiter<T> {
            return waitForEvent(cls, predicate, action, 0, null, null)
        }

        fun timeout(timeout: Long, unit: TimeUnit, timeoutAction: () -> Unit): Waiter<T> {
            return waitForEvent(cls, predicate, action, timeout, unit, timeoutAction)
        }

        fun timeout(timeout: Long, unit: TimeUnit, timeoutAction: Runnable): Waiter<T> {
            return waitForEvent(cls, predicate, action, timeout, unit, { timeoutAction.run() })
        }

        fun timeout(timeout: Long, unit: TimeUnit): Waiter<T> {
            return waitForEvent(cls, predicate, action, timeout, unit, null)
        }
    }

    inner class Waiter<in T : GenericEvent>(private val cls: Class<in T>,
                                            private val predicate: (T) -> Boolean,
                                            private val action: (T) -> Unit) {
        fun isValid(): Boolean {
            return waiters[cls]?.contains(this) == true
        }

        fun attempt(event: T): Boolean { // predicate(event) && action(event).let { true }
            return if (predicate(event)) {
                action(event)
                true
            } else {
                false
            }
        }

        fun cancel(): Boolean {
            return waiters[cls]?.remove(this) == true
        }
    }
}