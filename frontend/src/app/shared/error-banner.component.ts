import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-error-banner',
  standalone: true,
  template: `@if (message) {
    <div class="error-banner" role="alert">{{ message }}</div>
  }`,
  styles: [`
    .error-banner {
      background: #fef2f2; color: #991b1b; border: 1px solid #fecaca;
      border-radius: 8px; padding: 10px 14px; font-size: 0.9rem; margin: 8px 0;
    }
  `]
})
export class ErrorBannerComponent {
  @Input() message: string | null = null;
}
