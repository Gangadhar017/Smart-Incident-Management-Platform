import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../services/api.service';

@Component({
  selector: 'app-incident-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div *ngIf="loading" style="padding: 40px; text-align: center; color: var(--text-muted);">
      Loading incident details...
    </div>

    <div *ngIf="!loading && incident" class="split-view-container">
      <!-- Left Info/Collab Panel -->
      <div class="left-details-pane">
        <!-- Ticket Header -->
        <div style="background-color: #FFFFFF; border: 1px solid var(--border-color); border-radius: 8px; padding: 20px; box-shadow: 0 1px 2px rgba(0,0,0,0.05);">
          <div style="display: flex; gap: 8px; align-items: center; margin-bottom: 8px;">
            <span style="font-weight: 700; color: var(--primary); font-size: 15px;">{{ incident.incidentNumber }}</span>
            <span class="badge" [ngClass]="'badge-' + incident.priority.toLowerCase()">{{ incident.priority }}</span>
            <span class="badge" [ngClass]="'badge-' + incident.status.toLowerCase().replace('_', '-')">{{ incident.status.replace('_', ' ') }}</span>
            <span *ngIf="incident.slaBreached" class="badge badge-p1" style="background-color: #FEE2E2; color: var(--danger);">SLA BREACHED</span>
          </div>
          <h2 style="font-size: 18px; font-weight: 700; color: var(--text-main); margin-bottom: 12px;">{{ incident.title }}</h2>
          <div style="font-size: 13px; color: var(--text-muted); background: #F8FAFC; border-radius: 6px; padding: 12px; white-space: pre-wrap; font-family: inherit;">{{ incident.description }}</div>
        </div>

        <!-- Navigation Tabs -->
        <div class="tabs-container">
          <div class="tabs-header">
            <button (click)="activeTab = 'comments'" [class.active]="activeTab === 'comments'" class="tab-btn">Comments ({{ comments.length }})</button>
            <button (click)="activeTab = 'attachments'" [class.active]="activeTab === 'attachments'" class="tab-btn">Attachments ({{ attachments.length }})</button>
            <button (click)="activeTab = 'audit'" [class.active]="activeTab === 'audit'" class="tab-btn">Audit Trail ({{ auditLogs.length }})</button>
          </div>

          <div class="tab-content">
            <!-- Comments Tab -->
            <div *ngIf="activeTab === 'comments'" style="display: flex; flex-direction: column; gap: 16px;">
              <div class="comments-list">
                <div *ngFor="let comment of comments" class="comment-item" [style.border-left]="comment.internal ? '3px solid var(--warning)' : '3px solid var(--border-color)'">
                  <div style="display: flex; justify-content: space-between; font-size: 12px; margin-bottom: 4px;">
                    <span style="font-weight: 700;">{{ comment.authorName }}</span>
                    <span style="color: var(--text-muted);">{{ comment.createdDate | date:'yyyy-MM-dd HH:mm' }}</span>
                  </div>
                  <div style="font-size: 13px;" [innerHTML]="formatCommentContent(comment.content)"></div>
                  <span *ngIf="comment.internal" style="font-size: 10px; color: var(--warning); font-weight: 600; text-transform: uppercase;">Internal Worknote</span>
                </div>
                <div *ngIf="comments.length === 0" style="text-align: center; padding: 20px; color: var(--text-muted);">No comments posted yet.</div>
              </div>

              <!-- Post Comment Form -->
              <div style="background-color: #F8FAFC; padding: 16px; border-radius: 6px; border: 1px solid var(--border-color);">
                <textarea [(ngModel)]="newCommentContent" class="input-field" style="height: 80px; resize: none; margin-bottom: 8px;" placeholder="Type a comment... Use @username to mention engineers"></textarea>
                <div style="display: flex; justify-content: space-between; align-items: center;">
                  <label style="display: flex; align-items: center; gap: 6px; font-size: 12px; font-weight: 600; color: var(--text-muted);">
                    <input type="checkbox" [(ngModel)]="isCommentInternal" />
                    <span>Internal Note (Private to support agents)</span>
                  </label>
                  <button (click)="postComment()" [disabled]="!newCommentContent.trim()" class="btn btn-primary" style="padding: 6px 12px;">Submit</button>
                </div>
              </div>
            </div>

            <!-- Attachments Tab -->
            <div *ngIf="activeTab === 'attachments'" style="display: flex; flex-direction: column; gap: 16px;">
              <div class="attachments-list">
                <div *ngFor="let file of attachments" class="attachment-item">
                  <div style="display: flex; align-items: center; gap: 8px;">
                    <span class="material-icons" style="color: var(--text-muted);">description</span>
                    <div style="display: flex; flex-direction: column;">
                      <span style="font-weight: 600; font-size: 13px;">{{ file.filename }}</span>
                      <span style="font-size: 11px; color: var(--text-muted);">{{ formatBytes(file.fileSize) }} • by {{ file.uploadedByName }}</span>
                    </div>
                  </div>
                  <a [href]="apiService.downloadAttachmentUrl(file.id)" target="_blank" class="btn" style="padding: 4px 8px;">
                    <span class="material-icons" style="font-size: 16px;">download</span>
                  </a>
                </div>
                <div *ngIf="attachments.length === 0" style="text-align: center; padding: 20px; color: var(--text-muted);">No files uploaded.</div>
              </div>

              <!-- Upload Form -->
              <div style="border: 1px dashed var(--border-color); background: #F8FAFC; border-radius: 6px; padding: 16px; text-align: center;">
                <input type="file" (change)="onFileSelected($event)" #fileInput style="display: none;"/>
                <button (click)="fileInput.click()" class="btn">
                  <span class="material-icons">cloud_upload</span>
                  <span>Select Diagnostics Log/File</span>
                </button>
              </div>
            </div>

            <!-- Audit Trails Tab -->
            <div *ngIf="activeTab === 'audit'" style="display: flex; flex-direction: column; gap: 10px;">
              <div *ngFor="let audit of auditLogs" class="audit-item">
                <div style="display: flex; justify-content: space-between; font-size: 12px; margin-bottom: 2px;">
                  <span style="font-weight: 600; color: var(--primary);">{{ audit.action.toUpperCase() }}</span>
                  <span style="color: var(--text-muted);">{{ audit.timestamp | date:'yyyy-MM-dd HH:mm:ss' }}</span>
                </div>
                <div style="font-size: 12px; color: var(--text-main);">
                  Changed by: <strong>{{ audit.changedByName }}</strong>
                </div>
                <div *ngIf="audit.oldValue" style="font-size: 11px; color: var(--text-muted); margin-top: 2px;">
                  From <code style="background: #F1F5F9; padding: 2px 4px; border-radius: 4px;">{{ audit.oldValue }}</code> to <code style="background: #E2E8F0; padding: 2px 4px; border-radius: 4px;">{{ audit.newValue }}</code>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Actions/Metadata Panel -->
      <div class="right-meta-pane">
        <!-- SLA Countdowns -->
        <div style="background-color: #FAF5FF; border: 1px solid #E9D5FF; padding: 12px; border-radius: 6px; text-align: center;">
          <h4 style="font-size: 11px; text-transform: uppercase; color: #7C3AED; font-weight: 700; margin-bottom: 4px;">SLA Deadline Time Remaining</h4>
          <div [style.color]="incident.slaBreached ? 'var(--danger)' : '#7C3AED'" style="font-size: 20px; font-weight: 800;">
            {{ slaTimerString }}
          </div>
        </div>

        <!-- Workflow Action Statuses -->
        <div>
          <h4 style="font-size: 11px; text-transform: uppercase; color: var(--text-muted); font-weight: 700; margin-bottom: 8px;">State Operations</h4>
          <div style="display: flex; flex-direction: column; gap: 8px;">
            <button *ngIf="incident.status === 'ASSIGNED' || incident.status === 'OPEN'" (click)="updateStatus('IN_PROGRESS')" class="btn btn-primary" style="width: 100%;">Acknowledge & Work</button>
            <button *ngIf="incident.status === 'IN_PROGRESS' || incident.status === 'PENDING'" (click)="updateStatus('RESOLVED')" class="btn btn-primary" style="background: var(--success); border-color: var(--success); width: 100%;">Mark Resolved</button>
            <button *ngIf="incident.status === 'RESOLVED'" (click)="updateStatus('CLOSED')" class="btn" style="width: 100%;">Archive & Close</button>
            <button *ngIf="incident.status !== 'CLOSED' && incident.status !== 'CANCELLED'" (click)="updateStatus('CANCELLED')" class="btn btn-danger" style="width: 100%;">Cancel Incident</button>
          </div>
        </div>

        <!-- Meta Details Fields -->
        <div style="display: flex; flex-direction: column; gap: 8px;">
          <h4 style="font-size: 11px; text-transform: uppercase; color: var(--text-muted); font-weight: 700;">Properties</h4>
          
          <div class="meta-row">
            <span class="meta-label">Reporter</span>
            <span class="meta-value">{{ incident.reporterName }}</span>
          </div>

          <div class="meta-row" style="flex-direction: column; align-items: flex-start; gap: 4px; border: none; padding-bottom: 0;">
            <span class="meta-label">Assignee Owner</span>
            <select [ngModel]="incident.assigneeId" (change)="assignTicket($event)" class="input-field" style="margin-top: 4px;">
              <option [value]="null">Unassigned</option>
              <option *ngFor="let eng of staffEngineers" [value]="eng.id">{{ eng.username }} ({{ eng.role }})</option>
            </select>
          </div>

          <div class="meta-row">
            <span class="meta-label">Category</span>
            <span class="meta-value">{{ incident.category }}</span>
          </div>

          <div class="meta-row" *ngIf="incident.subcategory">
            <span class="meta-label">Subcategory</span>
            <span class="meta-value">{{ incident.subcategory }}</span>
          </div>

          <div class="meta-row">
            <span class="meta-label">Severity</span>
            <span class="meta-value">{{ incident.severity }}</span>
          </div>

          <div class="meta-row">
            <span class="meta-label">Created Date</span>
            <span class="meta-value" style="font-size: 11px;">{{ incident.createdDate | date:'yyyy-MM-dd HH:mm' }}</span>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .tabs-container {
      background-color: #FFFFFF;
      border: 1px solid var(--border-color);
      border-radius: 8px;
      overflow: hidden;
    }
    .tabs-header {
      display: flex;
      background-color: #F8FAFC;
      border-bottom: 1px solid var(--border-color);
    }
    .tab-btn {
      padding: 10px 16px;
