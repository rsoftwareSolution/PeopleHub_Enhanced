import { DOCUMENT } from '@angular/common';
import { DestroyRef, Injectable, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import {
  fromEvent,
  merge,
  Subscription,
  switchMap,
  timer
} from 'rxjs';

import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class InactivityService {
  private readonly document = inject(DOCUMENT);
  private readonly destroyRef = inject(DestroyRef);
  private readonly authService = inject(AuthService);

  private readonly timeoutMs = 30 * 60 * 1000;

  private inactivitySubscription?: Subscription;

  constructor() {
    // Stop monitoring when the user logs out.
    this.authService.loggedOut$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.stop();
      });
  }

  /**
   * Starts monitoring user activity.
   * Calls the supplied callback after 30 minutes of inactivity.
   */
  start(onTimeout: () => void): void {
    // Avoid creating multiple activity subscriptions.
    this.stop();

    const activityEvents = merge(
      fromEvent(this.document, 'mousemove'),
      fromEvent(this.document, 'mousedown'),
      fromEvent(this.document, 'click'),
      fromEvent(this.document, 'keydown'),
      fromEvent(this.document, 'scroll'),
      fromEvent(this.document, 'touchstart'),
      fromEvent(this.document, 'touchmove')
    );

    this.inactivitySubscription = activityEvents
      .pipe(
        switchMap(() => timer(this.timeoutMs))
      )
      .subscribe(() => {
        if (this.authService.isLoggedIn()) {
          this.stop();
          onTimeout();
        }
      });
  }

  /**
   * Stops inactivity monitoring.
   */
  stop(): void {
    this.inactivitySubscription?.unsubscribe();
    this.inactivitySubscription = undefined;
  }
}
