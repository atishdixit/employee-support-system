import { Component } from '@angular/core';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  template: `
    <div class="spinner" role="status" aria-label="Loading">
      <span class="dot"></span><span class="dot"></span><span class="dot"></span>
    </div>
  `,
  styles: [`
    .spinner { display: inline-flex; gap: 4px; align-items: center; padding: 4px 0; }
    .dot {
      width: 8px; height: 8px; border-radius: 50%; background: #6b7280;
      animation: bounce 1s infinite ease-in-out both;
    }
    .dot:nth-child(2) { animation-delay: 0.15s; }
    .dot:nth-child(3) { animation-delay: 0.3s; }
    @keyframes bounce {
      0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
      40% { transform: scale(1); opacity: 1; }
    }
  `]
})
export class LoadingSpinnerComponent {}
