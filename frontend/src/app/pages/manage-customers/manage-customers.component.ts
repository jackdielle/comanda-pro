import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslocoModule } from '@ngneat/transloco';
import { ApiService, Customer } from '../../services/api.service';

@Component({
  selector: 'app-manage-customers',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslocoModule],
  templateUrl: './manage-customers.component.html',
  styleUrls: ['./manage-customers.component.scss']
})
export class ManageCustomersComponent implements OnInit {
  customers: Customer[] = [];
  loading = true;
  error: string | null = null;
  successMessage: string | null = null;

  formVisible = false;
  customerBeingEdited: Customer | null = null;
  formData = this.createEmptyForm();

  constructor(private apiService: ApiService) { }

  ngOnInit(): void {
    this.loadCustomers();
  }

  loadCustomers(): void {
    this.loading = true;
    this.apiService.getAllCustomers().subscribe({
      next: (customers) => {
        this.customers = customers;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error loading customers';
        this.loading = false;
      }
    });
  }

  createEmptyForm(): Customer {
    return {
      phoneNumber: '',
      name: '',
      address: '',
      intercom: '',
      zone: ''
    };
  }

  openForm(customer?: Customer): void {
    if (customer) {
      this.customerBeingEdited = customer;
      this.formData = { ...customer };
    } else {
      this.customerBeingEdited = null;
      this.formData = this.createEmptyForm();
    }
    this.formVisible = true;
  }

  closeForm(): void {
    this.formVisible = false;
    this.formData = this.createEmptyForm();
    this.customerBeingEdited = null;
  }

  saveCustomer(): void {
    if (!this.formData.phoneNumber || !this.formData.name ||
      !this.formData.address || !this.formData.zone) {
      this.error = 'Please fill in all required fields';
      return;
    }

    if (this.customerBeingEdited) {
      this.apiService.updateCustomer(this.customerBeingEdited.id!, this.formData).subscribe({
        next: (updatedCustomer) => {
          const index = this.customers.findIndex(c => c.id === updatedCustomer.id);
          if (index > -1) {
            this.customers[index] = updatedCustomer;
          }
          this.successMessage = 'Customer updated successfully!';
          this.closeForm();
        },
        error: (err) => {
          this.error = 'Error updating customer';
        }
      });
    } else {
      this.apiService.createCustomer(this.formData).subscribe({
        next: (newCustomer) => {
          this.customers.unshift(newCustomer);
          this.successMessage = 'Customer created successfully!';
          this.closeForm();
        },
        error: (err) => {
          this.error = err.message;
        }
      });
    }
  }

  deleteCustomer(id: number | undefined): void {
    if (!id || !confirm('Are you sure you want to delete this customer?')) {
      return;
    }

    this.apiService.deleteCustomer(id).subscribe({
      next: () => {
        this.customers = this.customers.filter(c => c.id !== id);
        this.successMessage = 'Customer deleted successfully!';
      },
      error: (err) => {
        this.error = 'Error deleting customer';
      }
    });
  }
}
