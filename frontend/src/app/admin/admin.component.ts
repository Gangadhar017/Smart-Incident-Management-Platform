import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../services/api.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="admin-grid">
      <!-- Left Panel: Create User -->
      <div class="admin-card">
        <h3>Create New User Profile</h3>
        <p class="subtitle" style="margin-bottom: 16px;">Provision new organizational access with RBAC permissions</p>
        
        <form (ngSubmit)="saveUser()" #userForm="ngForm" style="display: flex; flex-direction: column; gap: 12px;">
          <div>
            <label class="form-label">Username</label>
            <input type="text" [(ngModel)]="newUser.username" name="username" required class="input-field" placeholder="e.g. jdoe"/>
          </div>

          <div>
            <label class="form-label">Email Address</label>
            <input type="email" [(ngModel)]="newUser.email" name="email" required class="input-field" placeholder="jdoe@enterprise.com"/>
          </div>

          <div>
            <label class="form-label">Password</label>
            <input type="password" [(ngModel)]="newUser.password" name="password" required class="input-field" placeholder="••••••••"/>
          </div>

          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
            <div>
              <label class="form-label">Role Privilege</label>
              <select [(ngModel)]="newUser.role" name="role" required class="input-field">
                <option value="EMPLOYEE">Employee</option>
                <option value="SUPPORT_ENGINEER">Support Engineer</option>
                <option value="TEAM_LEAD">Team Lead</option>
                <option value="INCIDENT_MANAGER">Incident Manager</option>
                <option value="ADMIN">Administrator</option>
              </select>
            </div>
            <div>
              <label class="form-label">Department</label>
              <select [(ngModel)]="newUser.departmentId" name="departmentId" (change)="loadTeams()" class="input-field">
                <option [value]="null">None</option>
                <option *ngFor="let dept of departments" [value]="dept.id">{{ dept.name }}</option>
              </select>
            </div>
          </div>

          <div>
            <label class="form-label">Assigned Team</label>
            <select [(ngModel)]="newUser.teamId" name="teamId" [disabled]="!newUser.departmentId" class="input-field">
              <option [value]="null">None</option>
              <option *ngFor="let team of teams" [value]="team.id">{{ team.name }}</option>
            </select>
          </div>

          <div>
            <label class="form-label">Skills (Comma Separated)</label>
            <input type="text" [(ngModel)]="skillsString" name="skills" class="input-field" placeholder="e.g. Linux, SQL, Kubernetes"/>
          </div>

          <button type="submit" [disabled]="!userForm.valid" class="btn btn-primary" style="margin-top: 10px; padding: 10px;">
            Provision User
          </button>
        </form>
      </div>

      <!-- Right Panel: Employee Directory -->
      <div class="admin-card">
        <h3>System Directory</h3>
        <p class="subtitle" style="margin-bottom: 16px;">View and search existing staff accounts across departments</p>
        
        <div style="margin-bottom: 12px;">
          <input type="text" [(ngModel)]="searchQuery" (ngModelChange)="loadDirectory()" class="input-field" placeholder="Search directory by username..."/>
        </div>

        <div class="data-table-wrapper" style="max-height: 400px; overflow-y: auto; margin-top: 0;">
          <table class="dense-table">
            <thead>
              <tr>
                <th>Username</th>
                <th>Email</th>
                <th>Role</th>
                <th>Department</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let staff of staffList">
                <td style="font-weight: 600;">{{ staff.username }}</td>
                <td>{{ staff.email }}</td>
                <td>
                  <span class="badge badge-p4" style="background: #F1F5F9; color: var(--text-main);">
                    {{ staff.role }}
                  </span>
                </td>
                <td>{{ staff.departmentName || 'N/A' }}</td>
              </tr>
