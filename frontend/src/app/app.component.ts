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
        </div>
      </aside>

      <!-- Main Panel Work Area -->
      <main class="main-content">
        <header class="top-nav">
          <div style="font-weight: 500; color: var(--text-muted);">Smart Incident Management Console v1.0</div>
          <div style="display: flex; align-items: center; gap: 8px;">
            <span class="material-icons" style="font-size: 20px; color: var(--text-muted);">notifications</span>
            <div style="width: 8px; height: 8px; background: var(--success); border-radius: 50%;"></div>
            <span style="font-size: 12px; font-weight: 600; color: var(--text-muted);">System Active</span>
          </div>
        </header>
        <div class="content-pane">
          <router-outlet></router-outlet>
        </div>
      </main>
    </div>

    <!-- Anonymous Flow Layout (Login Screen) -->
    <ng-template #anonymous>
      <div style="width: 100vw; height: 100vh; overflow: auto; background-color: var(--bg-main);">
        <router-outlet></router-outlet>
      </div>
    </ng-template>
  `
})
export class AppComponent {
  constructor(public apiService: ApiService, private router: Router) {
    // If not authenticated, force routing to login
    if (!this.apiService.isAuthenticated()) {
      this.router.navigate(['/login']);
    }
  }

  logout(): void {
    this.apiService.logout();
    this.router.navigate(['/login']);
  }
}
