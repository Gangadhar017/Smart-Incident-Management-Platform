import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ApiService } from './services/api.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="app-container" *ngIf="apiService.currentUser$ | async as user; else anonymous">
      <!-- Dark Enterprise Sidebar -->
      <aside class="sidebar">
        <div class="sidebar-header">
          <span class="material-icons" style="color: var(--primary);">shield</span>
          <span>Enterprise IMP</span>
        </div>
        <nav class="sidebar-menu">
          <a routerLink="/dashboard" routerLinkActive="active" class="sidebar-item">
            <span class="material-icons">dashboard</span>
            <span>Dashboard</span>
          </a>
          <a routerLink="/incidents" routerLinkActive="active" class="sidebar-item">
            <span class="material-icons">confirmation_number</span>
            <span>Incident Board</span>
          </a>
          <a *ngIf="user.role === 'ADMIN' || user.role === 'SUPER_ADMIN'" routerLink="/admin" routerLinkActive="active" class="sidebar-item">
            <span class="material-icons">admin_panel_settings</span>
            <span>Admin Settings</span>
          </a>
        </nav>
        <div class="sidebar-footer">
          <div style="display: flex; flex-direction: column; gap: 2px;">
            <span style="font-weight: 600; font-size: 13px;">{{ user.username }}</span>
            <span style="font-size: 11px; color: #9CA3AF;">{{ user.role }}</span>
          </div>
          <button (click)="logout()" class="btn" style="padding: 4px; min-width: auto; border: none; background: transparent; color: #9CA3AF;">
            <span class="material-icons">logout</span>
          </button>
