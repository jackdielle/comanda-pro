import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TranslocoModule } from '@ngneat/transloco';
import { ApiService } from '../../services/api.service';

interface User {
  id: number;
  username: string;
  role: string;
  enabled: boolean;
  createdAt: string;
}

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, TranslocoModule],
  templateUrl: './admin-users.component.html',
  styleUrls: ['./admin-users.component.scss']
})
export class AdminUsersComponent implements OnInit {
  users: User[] = [];
  createUserForm!: FormGroup;
  resetPasswordForm!: FormGroup;
  showCreateForm = false;
  showResetPasswordForm = false;
  selectedUserId: number | null = null;
  loading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  constructor(
    private apiService: ApiService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.initializeForms();
    this.loadUsers();
  }

  initializeForms(): void {
    this.createUserForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(8)]],
      role: ['ROLE_MANAGER', Validators.required]
    });

    this.resetPasswordForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(group: FormGroup): { [key: string]: any } | null {
    const newPassword = group.get('newPassword')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return newPassword === confirmPassword ? null : { passwordMismatch: true };
  }

  loadUsers(): void {
    this.apiService.getAllUsers().subscribe({
      next: (data) => {
        this.users = data;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load users';
      }
    });
  }

  createUser(): void {
    if (this.createUserForm.invalid) {
      return;
    }

    this.loading = true;
    this.errorMessage = null;
    this.successMessage = null;

    const request = {
      username: this.createUserForm.get('username')!.value,
      password: this.createUserForm.get('password')!.value,
      role: this.createUserForm.get('role')!.value
    };

    this.apiService.createUser(request).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'User created successfully';
        this.createUserForm.reset({ role: 'ROLE_MANAGER' });
        this.showCreateForm = false;
        this.loadUsers();
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Failed to create user';
      }
    });
  }

  toggleEnabled(userId: number): void {
    this.apiService.toggleUser(userId).subscribe({
      next: () => {
        this.successMessage = 'User status updated';
        this.loadUsers();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to update user';
      }
    });
  }

  deleteUser(userId: number): void {
    if (!confirm('Are you sure you want to delete this user?')) {
      return;
    }

    this.apiService.deleteUser(userId).subscribe({
      next: () => {
        this.successMessage = 'User deleted successfully';
        this.loadUsers();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to delete user';
      }
    });
  }

  openResetPasswordForm(userId: number): void {
    this.selectedUserId = userId;
    this.showResetPasswordForm = true;
    this.resetPasswordForm.reset();
  }

  resetPassword(): void {
    if (this.resetPasswordForm.invalid || !this.selectedUserId) {
      return;
    }

    this.loading = true;
    this.errorMessage = null;

    const request = {
      newPassword: this.resetPasswordForm.get('newPassword')!.value
    };

    this.apiService.resetUserPassword(this.selectedUserId, request).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Password reset successfully';
        this.showResetPasswordForm = false;
        this.resetPasswordForm.reset();
        this.selectedUserId = null;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Failed to reset password';
      }
    });
  }

  closeForm(): void {
    this.showCreateForm = false;
    this.showResetPasswordForm = false;
    this.selectedUserId = null;
    this.createUserForm.reset({ role: 'ROLE_MANAGER' });
    this.resetPasswordForm.reset();
  }
}
