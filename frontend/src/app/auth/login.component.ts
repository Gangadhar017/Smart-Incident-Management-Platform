import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../services/api.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="login-wrapper">
      <div class="login-card">
        <div class="login-header">
          <span class="material-icons" style="font-size: 32px; color: var(--primary);">shield</span>
          <h2>Enterprise Portal</h2>
          <p>Smart Incident Management Platform</p>
        </div>
        
        <form (ngSubmit)="onSubmit()" #loginForm="ngForm" style="display: flex; flex-direction: column; gap: 16px;">
          <div>
            <label class="form-label">Username</label>
            <input 
              type="text" 
              name="username" 
              [(ngModel)]="credentials.username" 
              required 
              class="input-field" 
              placeholder="e.g. superadmin, engineer, employee"
            />
          </div>
          
          <div>
            <label class="form-label">Password</label>
            <input 
              type="password" 
              name="password" 
              [(ngModel)]="credentials.password" 
              required 
              class="input-field" 
              placeholder="••••••••"
            />
          </div>

          <div *ngIf="errorMessage" class="error-box">
            {{ errorMessage }}
          </div>

          <button type="submit" [disabled]="!loginForm.valid" class="btn btn-primary" style="padding: 10px; width: 100%;">
            Sign In
          </button>
        </form>

        <div class="login-footer">
          <p>Demo users password: <strong>adminpassword</strong></p>
          <div style="font-size: 11px; margin-top: 10px; color: var(--text-muted);">
            Roles pre-seeded: superadmin, admin, manager, lead, engineer, employee
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .login-wrapper {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      background-color: #F1F5F9;
      padding: 20px;
    }
    .login-card {
      background: #FFFFFF;
      border: 1px solid var(--border-color);
      border-radius: 8px;
      padding: 32px;
      width: 100%;
      max-width: 400px;
      box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05), 0 2px 4px -1px rgba(0,0,0,0.03);
    }
    .login-header {
      text-align: center;
      margin-bottom: 24px;
    }
    .login-header h2 {
      font-size: 20px;
      font-weight: 700;
      color: var(--text-main);
      margin-top: 8px;
    }
    .login-header p {
      font-size: 13px;
      color: var(--text-muted);
    }
    .form-label {
      display: block;
      font-size: 12px;
      font-weight: 600;
      color: var(--text-main);
      margin-bottom: 6px;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }
    .error-box {
      background-color: #FEF2F2;
      color: var(--danger);
      border: 1px solid #FCA5A5;
      padding: 10px;
      border-radius: 6px;
      font-size: 12px;
      text-align: center;
    }
    .login-footer {
      margin-top: 24px;
      padding-top: 16px;
      border-top: 1px solid var(--border-color);
      font-size: 12px;
      text-align: center;
      color: var(--text-main);
    }
  `]
})
export class LoginComponent {
  credentials = { username: '', password: '' };
  errorMessage = '';

  constructor(private apiService: ApiService, private router: Router) {
    if (this.apiService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
    }
  }

  onSubmit(): void {
    this.errorMessage = '';
    this.apiService.login(this.credentials).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Invalid username or password';
      }
    });
  }
}
