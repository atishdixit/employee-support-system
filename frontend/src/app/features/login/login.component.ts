import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { ErrorBannerComponent } from '../../shared/error-banner.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, ErrorBannerComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  username = '';
  password = '';
  loading = false;
  errorMessage: string | null = null;

  constructor(private readonly authService: AuthService) {}

  submit(): void {
    if (!this.username.trim() || !this.password.trim() || this.loading) {
      return;
    }
    this.loading = true;
    this.errorMessage = null;

    // AppComponent reacts to AuthService's `employee` signal, so no output event is needed here.
    this.authService.login({ username: this.username.trim(), password: this.password }).subscribe({
      next: () => {
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err?.error?.message ?? 'Login failed. Check your username and password.';
      }
    });
  }
}
