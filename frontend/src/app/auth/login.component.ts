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
