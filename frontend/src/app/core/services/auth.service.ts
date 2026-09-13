import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse } from '../models/auth.model';
import { Employee } from '../models/employee.model';

const TOKEN_KEY = 'ess.token';
const EMPLOYEE_KEY = 'ess.employee';

/**
 * Holds the JWT + logged-in employee in sessionStorage (cleared when the tab closes) so a page
 * reload doesn't lose the session, without persisting it beyond the browser session.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly baseUrl = `${environment.apiBaseUrl}/auth`;

  private readonly employeeSignal = signal<Employee | null>(this.readStoredEmployee());

  readonly employee = this.employeeSignal.asReadonly();

  constructor(private readonly http: HttpClient) {}

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/login`, request).pipe(
      tap((response) => {
        sessionStorage.setItem(TOKEN_KEY, response.token);
        sessionStorage.setItem(EMPLOYEE_KEY, JSON.stringify(response.employee));
        this.employeeSignal.set(response.employee);
      })
    );
  }

  logout(): void {
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(EMPLOYEE_KEY);
    this.employeeSignal.set(null);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getToken(): string | null {
    return sessionStorage.getItem(TOKEN_KEY);
  }

  private readStoredEmployee(): Employee | null {
    const raw = sessionStorage.getItem(EMPLOYEE_KEY);
    return raw ? (JSON.parse(raw) as Employee) : null;
  }
}
