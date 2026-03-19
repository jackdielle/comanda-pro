import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslocoModule } from '@ngneat/transloco';
import { ApiService, OrdersSummary } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, TranslocoModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  summary: OrdersSummary | null = null;
  loading = true;
  error: string | null = null;

  constructor(private apiService: ApiService, private authService: AuthService) { }

  ngOnInit(): void {
    this.loadSummary();
  }

  loadSummary(): void {
    this.apiService.getOrdersSummary().subscribe({
      next: (data) => {
        this.summary = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error loading data';
        this.loading = false;
      }
    });
  }

  get formattedRevenue(): string {
    return this.summary?.totalRevenue.toFixed(2) || '0.00';
  }

  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }
}
