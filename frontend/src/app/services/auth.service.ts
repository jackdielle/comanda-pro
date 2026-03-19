import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of, Subject } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface AuthUser {
  username: string;
  role: string;
  userId: number;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  username: string;
  role: string;
  userId: number;
}

export interface RefreshRequest {
  refreshToken: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService implements OnDestroy {
  private apiUrl = environment.apiUrl;
  private currentUserSubject = new BehaviorSubject<AuthUser | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  private destroy$ = new Subject<void>();
  private tokenRefreshTimer: any;

  constructor(private http: HttpClient) {
    this.loadUserFromStorage();
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/login`, credentials).pipe(
      tap((response: AuthResponse) => {
        this.setTokens(response);
        this.startTokenRefreshTimer();
        this.currentUserSubject.next({
          username: response.username,
          role: response.role,
          userId: response.userId
        });
      })
    );
  }

  logout(): Observable<void> {
    this.stopTokenRefreshTimer();
    return this.http.post<void>(`${this.apiUrl}/auth/logout`, {}).pipe(
      tap(() => this.clearTokens()),
      catchError(() => {
        this.clearTokens();
        return of(undefined);
      })
    );
  }

  refresh(refreshToken: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/refresh`, { refreshToken } as RefreshRequest).pipe(
      tap((response: AuthResponse) => {
        this.setTokens(response);
        this.currentUserSubject.next({
          username: response.username,
          role: response.role,
          userId: response.userId
        });
      })
    );
  }

  changePassword(request: ChangePasswordRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/auth/change-password`, request);
  }

  getAccessToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  getRefreshToken(): string | null {
    return localStorage.getItem('refreshToken');
  }

  isLoggedIn(): boolean {
    return !!this.getAccessToken();
  }

  isAdmin(): boolean {
    const user = this.currentUserSubject.value;
    return user?.role === 'ROLE_ADMIN';
  }

  getCurrentUser(): AuthUser | null {
    return this.currentUserSubject.value;
  }

  /**
   * Check if access token is expired
   */
  isTokenExpired(): boolean {
    const token = this.getAccessToken();
    if (!token) return true;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expirationTime = payload.exp * 1000; // exp is in seconds
      return Date.now() >= expirationTime;
    } catch (e) {
      return true;
    }
  }

  /**
   * Check if token is about to expire (within 1 minute)
   */
  isTokenExpiringSoon(): boolean {
    const token = this.getAccessToken();
    if (!token) return true;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expirationTime = payload.exp * 1000; // exp is in seconds
      const timeUntilExpiry = expirationTime - Date.now();
      return timeUntilExpiry < 60000; // Less than 1 minute
    } catch (e) {
      return true;
    }
  }

  /**
   * Proactively refresh token before expiration
   */
  private startTokenRefreshTimer(): void {
    this.stopTokenRefreshTimer();

    try {
      const token = this.getAccessToken();
      if (!token) return;

      const payload = JSON.parse(atob(token.split('.')[1]));
      const expirationTime = payload.exp * 1000;
      const timeUntilExpiry = expirationTime - Date.now();

      // Refresh 1 minute before expiration
      const refreshTime = Math.max(timeUntilExpiry - 60000, 5000); // At least 5 seconds

      this.tokenRefreshTimer = setTimeout(() => {
        this.refreshTokenSilently();
      }, refreshTime);
    } catch (e) {
      console.error('Error setting token refresh timer:', e);
    }
  }

  /**
   * Stop the token refresh timer
   */
  private stopTokenRefreshTimer(): void {
    if (this.tokenRefreshTimer) {
      clearTimeout(this.tokenRefreshTimer);
      this.tokenRefreshTimer = null;
    }
  }

  /**
   * Silently refresh the token (used proactively before expiration)
   */
  private refreshTokenSilently(): void {
    const refreshToken = this.getRefreshToken();
    if (refreshToken && !this.isTokenExpired()) {
      this.refresh(refreshToken).subscribe(
        () => {
          console.log('Token refreshed proactively');
          this.startTokenRefreshTimer(); // Start a new timer
        },
        (error) => {
          console.error('Failed to refresh token:', error);
          this.logout().subscribe();
        }
      );
    }
  }

  ngOnDestroy(): void {
    this.stopTokenRefreshTimer();
    this.destroy$.next();
    this.destroy$.complete();
  }

  private setTokens(response: AuthResponse): void {
    localStorage.setItem('accessToken', response.accessToken);
    localStorage.setItem('refreshToken', response.refreshToken);
  }

  private clearTokens(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    this.currentUserSubject.next(null);
  }

  private loadUserFromStorage(): void {
    const token = localStorage.getItem('accessToken');
    if (token) {
      // Try to decode JWT and extract user info (basic implementation)
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));

        // Check if token is already expired
        const expirationTime = payload.exp * 1000;
        if (Date.now() < expirationTime) {
          this.currentUserSubject.next({
            username: payload.sub,
            role: payload.role,
            userId: payload.userId
          });
          // Start token refresh timer for existing sessions
          this.startTokenRefreshTimer();
        } else {
          this.clearTokens();
        }
      } catch (e) {
        this.clearTokens();
      }
    }
  }
}
