/*
 * Copyright (c) 2011-2013 GoPivotal, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactor.core.action;

import reactor.core.Observable;
import reactor.event.Event;
import reactor.event.registry.Registration;
import reactor.function.Consumer;
import reactor.timer.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Stephane Maldini
 * @since 1.1
 */
public class CollectWithTimeoutAction<T> extends CollectAction<T> {

	private final Timer timer;
	private final long  timeout;
	private final Consumer<Long> timeoutTask = new Consumer<Long>() {
		@Override
		public void accept(Long aLong) {
			doFlush(null);
		}
	};

	private Registration<? extends Consumer<Long>> timeoutRegistration;

	public CollectWithTimeoutAction(int batchsize, Observable d, Object successKey, Object failureKey,
	                                Timer timer, long timeout) {
		super(batchsize, d, successKey, failureKey);
		this.timer = timer;
		this.timeout = timeout;
		timeoutRegistration = timer.submit(timeoutTask, timeout, TimeUnit.MILLISECONDS);
	}

	@Override
	public void doFlush(Event<T> ev) {
		timeoutRegistration.cancel();
		super.doFlush(ev);
		timeoutRegistration = timer.submit(timeoutTask, timeout, TimeUnit.MILLISECONDS);
	}
}
