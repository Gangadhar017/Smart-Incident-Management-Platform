import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../services/api.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div style="display: flex; flex-direction: column; gap: 20px;">
      <!-- Title Page -->
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
          <h1>Operations Dashboard</h1>
          <p class="subtitle">Real-time KPI monitor, MTTR trackers, and SLA compliance metrics</p>
        </div>
        <div style="display: flex; gap: 8px;">
          <button (click)="exportExcel()" class="btn">
            <span class="material-icons">table_view</span>
            <span>Export CSV</span>
          </button>
          <button (click)="exportPdf()" class="btn btn-primary">
            <span class="material-icons">picture_as_pdf</span>
            <span>Export Report</span>
          </button>
        </div>
      </div>

      <!-- Loading Tracker -->
      <div *ngIf="loading" style="padding: 40px; text-align: center; color: var(--text-muted);">
        Loading metrics dashboard data...
      </div>

      <ng-container *ngIf="!loading && kpis">
        <!-- KPI Cards Grid -->
        <div class="kpi-grid">
          <div class="kpi-card" style="border-left: 4px solid var(--primary);">
            <div class="kpi-title">Active Open Incidents</div>
            <div class="kpi-value">{{ kpis.openIncidents }}</div>
          </div>
          <div class="kpi-card" style="border-left: 4px solid var(--danger);">
            <div class="kpi-title">Critical (P1) Active</div>
            <div class="kpi-value">{{ kpis.criticalIncidents }}</div>
          </div>
          <div class="kpi-card" style="border-left: 4px solid var(--warning);">
            <div class="kpi-title">SLA Breaches</div>
            <div class="kpi-value">{{ kpis.slaBreaches }}</div>
          </div>
          <div class="kpi-card" style="border-left: 4px solid var(--success);">
            <div class="kpi-title">Resolution Rate</div>
            <div class="kpi-value">{{ kpis.resolutionRate }}%</div>
          </div>
          <div class="kpi-card" style="border-left: 4px solid #7C3AED;">
            <div class="kpi-title">MTTR (Resolution)</div>
            <div class="kpi-value">{{ kpis.mttrHours }}h</div>
          </div>
          <div class="kpi-card" style="border-left: 4px solid #06B6D4;">
            <div class="kpi-title">MTTA (Ack)</div>
            <div class="kpi-value">{{ kpis.mttaHours }}h</div>
          </div>
        </div>

        <!-- Distributions Panel -->
        <div class="chart-layout-grid">
          <!-- Priority Distribution -->
          <div class="chart-card">
            <h3>Priority Breakdown</h3>
            <p class="subtitle" style="margin-bottom: 16px;">Breakdown of active incidents by organizational priority</p>
            <div style="display: flex; flex-direction: column; gap: 12px;">
              <div *ngFor="let item of kpis.priorityDistribution | keyvalue">
                <div style="display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 4px;">
                  <span style="font-weight: 600;">{{ item.key }}</span>
                  <span style="color: var(--text-muted);">{{ item.value }} tickets</span>
                </div>
                <div class="bar-container">
                  <div class="bar-fill" [style.width.%]="getPercentage(item.value, totalIncidents)" 
                       [style.background-color]="getPriorityColor(item.key)"></div>
                </div>
              </div>
            </div>
          </div>

          <!-- Status Distribution -->
          <div class="chart-card">
            <h3>Lifecycle Stages</h3>
            <p class="subtitle" style="margin-bottom: 16px;">Current count of incidents per stage of the workflow</p>
            <div style="display: flex; flex-direction: column; gap: 12px;">
              <div *ngFor="let item of kpis.statusDistribution | keyvalue">
                <div style="display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 4px;">
                  <span style="font-weight: 600; text-transform: capitalize;">{{ item.key.replace('_', ' ').toLowerCase() }}</span>
                  <span style="color: var(--text-muted);">{{ item.value }} tickets</span>
                </div>
                <div class="bar-container">
                  <div class="bar-fill" [style.width.%]="getPercentage(item.value, totalIncidents)" 
                       style="background-color: #64748B;"></div>
                </div>
