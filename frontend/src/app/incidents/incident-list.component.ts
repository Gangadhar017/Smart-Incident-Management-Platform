import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../services/api.service';

@Component({
  selector: 'app-incident-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div style="display: flex; flex-direction: column; gap: 16px; height: 100%;">
      <!-- Board Header -->
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
          <h1>Incident Registry</h1>
          <p class="subtitle">Track production outages, active support tickets, and service-level agreements</p>
        </div>
        <button (click)="openDrawer()" class="btn btn-primary">
          <span class="material-icons">add</span>
          <span>New Incident</span>
        </button>
      </div>

      <!-- Filters Ribbon -->
      <div class="filters-ribbon">
        <div style="flex: 1; min-width: 200px;">
          <input 
            type="text" 
            [(ngModel)]="filters.search" 
            (ngModelChange)="applyFilters()" 
            class="input-field" 
            placeholder="Search by title, number..."
          />
        </div>
        
        <div style="width: 130px;">
          <select [(ngModel)]="filters.priority" (change)="applyFilters()" class="input-field">
            <option value="">All Priorities</option>
            <option value="P1">P1 Critical</option>
            <option value="P2">P2 High</option>
            <option value="P3">P3 Medium</option>
            <option value="P4">P4 Low</option>
          </select>
        </div>

        <div style="width: 140px;">
          <select [(ngModel)]="filters.status" (change)="applyFilters()" class="input-field">
            <option value="">All Statuses</option>
            <option value="OPEN">Open</option>
            <option value="ASSIGNED">Assigned</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="PENDING">Pending</option>
            <option value="RESOLVED">Resolved</option>
            <option value="CLOSED">Closed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>
      </div>

      <!-- Data Table -->
      <div class="data-table-wrapper" style="flex: 1; overflow-y: auto;">
        <table class="dense-table">
          <thead>
            <tr>
              <th style="width: 100px;">Number</th>
              <th>Title</th>
              <th style="width: 100px;">Priority</th>
              <th style="width: 120px;">Status</th>
              <th style="width: 140px;">Assignee</th>
              <th style="width: 160px;">SLA Due</th>
              <th style="width: 100px;">Breach</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let inc of incidents" (click)="viewIncident(inc.id)">
              <td style="font-weight: 700; color: var(--primary);">{{ inc.incidentNumber }}</td>
              <td style="font-weight: 500;">{{ inc.title }}</td>
              <td>
                <span class="badge" [ngClass]="'badge-' + inc.priority.toLowerCase()">
                  {{ inc.priority }}
                </span>
              </td>
              <td>
                <span class="badge" [ngClass]="'badge-' + inc.status.toLowerCase().replace('_', '-')">
                  {{ inc.status.replace('_', ' ') }}
                </span>
              </td>
              <td>{{ inc.assigneeName }}</td>
              <td>{{ inc.slaDueDate | date:'yyyy-MM-dd HH:mm' }}</td>
              <td>
                <span [style.color]="inc.slaBreached ? 'var(--danger)' : 'var(--success)'" style="font-weight: 700;">
                  {{ inc.slaBreached ? 'BREACHED' : 'ACTIVE' }}
                </span>
              </td>
            </tr>
            <tr *ngIf="incidents.length === 0">
              <td colspan="7" style="text-align: center; padding: 32px; color: var(--text-muted);">
                No incidents match the search criteria.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Table Footer / Pagination -->
      <div style="display: flex; justify-content: space-between; align-items: center; padding: 4px 8px;">
        <span style="font-size: 12px; color: var(--text-muted);">Total elements: {{ totalElements }}</span>
        <div style="display: flex; gap: 8px;">
          <button (click)="prevPage()" [disabled]="filters.page === 0" class="btn" style="padding: 4px 8px;">
            <span class="material-icons" style="font-size: 18px;">chevron_left</span>
          </button>
          <button (click)="nextPage()" [disabled]="(filters.page + 1) * filters.size >= totalElements" class="btn" style="padding: 4px 8px;">
            <span class="material-icons" style="font-size: 18px;">chevron_right</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Create Side Drawer Modal -->
    <div class="drawer-overlay" *ngIf="drawerOpen" (click)="closeDrawer()">
      <div class="drawer-content" (click)="$event.stopPropagation()">
        <div class="drawer-header">
          <h3>Create New Incident</h3>
          <button (click)="closeDrawer()" class="btn" style="border: none; padding: 4px;">
            <span class="material-icons">close</span>
          </button>
        </div>
        <form (ngSubmit)="saveIncident()" style="display: flex; flex-direction: column; gap: 16px; padding: 20px;">
          <div>
            <label class="drawer-label">Title</label>
            <input type="text" [(ngModel)]="newIncident.title" name="title" required class="input-field" placeholder="Provide a summary of the issue"/>
          </div>
          <div>
            <label class="drawer-label">Description</label>
            <textarea [(ngModel)]="newIncident.description" name="description" required class="input-field" style="height: 100px; resize: none;" placeholder="Detail the replication steps and system symptoms"></textarea>
          </div>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
            <div>
              <label class="drawer-label">Category</label>
              <select [(ngModel)]="newIncident.category" name="category" required class="input-field">
                <option value="Hardware">Hardware</option>
                <option value="Software">Software</option>
                <option value="Network">Network</option>
                <option value="Database">Database</option>
                <option value="Security">Security</option>
              </select>
            </div>
            <div>
              <label class="drawer-label">Subcategory</label>
              <input type="text" [(ngModel)]="newIncident.subcategory" name="subcategory" class="input-field" placeholder="e.g. Latency, Failure"/>
            </div>
          </div>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
            <div>
              <label class="drawer-label">Priority</label>
              <select [(ngModel)]="newIncident.priority" name="priority" required class="input-field">
                <option value="P1">P1 Critical</option>
