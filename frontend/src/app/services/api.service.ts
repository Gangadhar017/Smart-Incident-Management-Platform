import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private gatewayUrl = 'http://localhost:8080/api/v1';
  
  private currentUserSubject = new BehaviorSubject<any>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    const cachedUser = localStorage.getItem('user_details');
    if (cachedUser) {
      this.currentUserSubject.next(JSON.parse(cachedUser));
    }
  }

  // Authentication
  login(credentials: any): Observable<any> {
    return this.http.post(`${this.gatewayUrl}/auth/login`, credentials).pipe(
      tap((res: any) => {
        localStorage.setItem('access_token', res.accessToken);
        localStorage.setItem('refresh_token', res.refreshToken);
        const user = {
          id: res.userId,
          username: res.username,
          email: res.email,
          role: res.role
        };
        localStorage.setItem('user_details', JSON.stringify(user));
        this.currentUserSubject.next(user);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('user_details');
    this.currentUserSubject.next(null);
  }

  getCurrentUser(): any {
    return this.currentUserSubject.value;
  }

  isAuthenticated(): boolean {
    return localStorage.getItem('access_token') !== null;
  }

  // User Management
  createUser(userRequest: any): Observable<any> {
    return this.http.post(`${this.gatewayUrl}/users`, userRequest);
  }

  getUsers(search?: string, departmentId?: number, page = 0, size = 50): Observable<any> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (search) params = params.set('search', search);
    if (departmentId) params = params.set('departmentId', departmentId);
    return this.http.get(`${this.gatewayUrl}/users`, { params });
  }

  // Departments & Teams
  getDepartments(): Observable<any> {
    return this.http.get(`${this.gatewayUrl}/departments`);
  }

  getTeamsByDepartment(departmentId: number): Observable<any> {
    return this.http.get(`${this.gatewayUrl}/teams/department/${departmentId}`);
  }

  // Incidents Module
  getIncidents(filters: {
    search?: string;
    priority?: string;
    status?: string;
    assigneeId?: number;
    departmentId?: number;
    page: number;
    size: number;
    sortBy: string;
    direction: string;
  }): Observable<any> {
    let params = new HttpParams()
      .set('page', filters.page)
      .set('size', filters.size)
      .set('sortBy', filters.sortBy)
      .set('direction', filters.direction);

    if (filters.search) params = params.set('search', filters.search);
    if (filters.priority) params = params.set('priority', filters.priority);
    if (filters.status) params = params.set('status', filters.status);
    if (filters.assigneeId) params = params.set('assigneeId', filters.assigneeId);
    if (filters.departmentId) params = params.set('departmentId', filters.departmentId);

    return this.http.get(`${this.gatewayUrl}/incidents`, { params });
  }

  getIncidentById(id: number): Observable<any> {
    return this.http.get(`${this.gatewayUrl}/incidents/${id}`);
  }

  getIncidentAudits(incidentId: number): Observable<any> {
    return this.http.get(`${this.gatewayUrl}/incidents/${incidentId}/audit`);
  }

  createIncident(incident: any): Observable<any> {
    return this.http.post(`${this.gatewayUrl}/incidents`, incident);
  }

  updateIncident(id: number, updates: any): Observable<any> {
    return this.http.put(`${this.gatewayUrl}/incidents/${id}`, updates);
  }

  // Comments
  getComments(incidentId: number): Observable<any> {
    return this.http.get(`${this.gatewayUrl}/comments/incident/${incidentId}`);
  }

  addComment(incidentId: number, comment: { content: string, isInternal: boolean }): Observable<any> {
    return this.http.post(`${this.gatewayUrl}/comments/incident/${incidentId}`, comment);
  }

  // Attachments
  getAttachments(incidentId: number): Observable<any> {
    return this.http.get(`${this.gatewayUrl}/attachments/incident/${incidentId}`);
  }

  uploadAttachment(incidentId: number, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.gatewayUrl}/attachments/incident/${incidentId}`, formData);
  }

  downloadAttachmentUrl(id: number): string {
    return `${this.gatewayUrl}/attachments/${id}/download`;
  }

  // Dashboard Analytics
  getDashboardKpis(): Observable<any> {
    return this.http.get(`${this.gatewayUrl}/analytics/dashboard`);
  }

  getReportPdf(): Observable<Blob> {
    return this.http.get(`${this.gatewayUrl}/reports/pdf`, { responseType: 'blob' });
  }

  getReportExcel(): Observable<Blob> {
    return this.http.get(`${this.gatewayUrl}/reports/excel`, { responseType: 'blob' });
  }
}
