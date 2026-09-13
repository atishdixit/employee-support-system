import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PolicyChatRequest, PolicyChatResponse } from '../models/policy-chat.model';

@Injectable({ providedIn: 'root' })
export class PolicyChatService {
  private readonly baseUrl = `${environment.apiBaseUrl}/policy/chat`;

  constructor(private readonly http: HttpClient) {}

  ask(request: PolicyChatRequest, debug = false): Observable<PolicyChatResponse> {
    const params = new HttpParams().set('debug', debug);
    return this.http.post<PolicyChatResponse>(this.baseUrl, request, { params });
  }
}
