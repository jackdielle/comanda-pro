import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslocoModule } from '@ngneat/transloco';
import { ApiService, DailyOrderSummary } from '../../services/api.service';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslocoModule],
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.scss']
})
export class ReportsComponent implements OnInit {
  startDate: string = '';
  endDate: string = '';
  reportData: DailyOrderSummary[] = [];
  loading = false;
  error: string | null = null;

  totals = {
    totalOrders: 0,
    totalRevenue: 0,
    totalClassic: 0,
    totalPanozzi: 0,
    totalRolled: 0,
    totalPinse: 0
  };

  constructor(private apiService: ApiService) { }

  ngOnInit(): void {
    const today = new Date().toISOString().split('T')[0];
    this.startDate = today;
    this.endDate = today;
  }

  generateReport(): void {
    if (!this.startDate || !this.endDate) {
      this.error = 'Please select both start and end dates';
      return;
    }

    this.loading = true;
    this.error = null;
    this.reportData = [];
    this.resetTotals();

    this.apiService.getOrdersSummaryByDateRange(this.startDate, this.endDate).subscribe({
      next: (data) => {
        this.reportData = data;
        this.calculateTotals();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error loading report data';
        this.loading = false;
      }
    });
  }

  private calculateTotals(): void {
    this.reportData.forEach(day => {
      this.totals.totalOrders += day.totalOrders;
      this.totals.totalRevenue += day.totalRevenue;
      this.totals.totalClassic += day.totalClassic;
      this.totals.totalPanozzi += day.totalPanozzi;
      this.totals.totalRolled += day.totalRolled;
      this.totals.totalPinse += day.totalPinse;
    });
  }

  private resetTotals(): void {
    this.totals = {
      totalOrders: 0,
      totalRevenue: 0,
      totalClassic: 0,
      totalPanozzi: 0,
      totalRolled: 0,
      totalPinse: 0
    };
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString + 'T00:00:00');
    return date.toLocaleDateString('it-IT', { weekday: 'short', year: 'numeric', month: '2-digit', day: '2-digit' });
  }

  formatRevenue(value: number): string {
    return value.toFixed(2);
  }
}
