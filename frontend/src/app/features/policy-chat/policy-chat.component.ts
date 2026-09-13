import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PolicyChatService } from '../../core/services/policy-chat.service';
import { ChatMessage } from '../../core/models/policy-chat.model';
import { EmployeeSelectorComponent } from '../employee-selector/employee-selector.component';
import { ChatMessageListComponent } from './chat-message-list.component';
import { ChatInputComponent } from './chat-input.component';
import { ErrorBannerComponent } from '../../shared/error-banner.component';

@Component({
  selector: 'app-policy-chat',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    EmployeeSelectorComponent,
    ChatMessageListComponent,
    ChatInputComponent,
    ErrorBannerComponent
  ],
  templateUrl: './policy-chat.component.html',
  styleUrl: './policy-chat.component.scss'
})
export class PolicyChatComponent {
  selectedEmployeeId = '';
  messages: ChatMessage[] = [];
  waitingForAnswer = false;
  errorMessage: string | null = null;
  debugMode = false;

  constructor(private readonly policyChatService: PolicyChatService) {}

  onEmployeeChange(employeeId: string): void {
    this.selectedEmployeeId = employeeId;
  }

  onAsk(question: string): void {
    if (!this.selectedEmployeeId) {
      this.errorMessage = 'Select an employee before asking a question.';
      return;
    }

    this.errorMessage = null;
    this.messages = [...this.messages, { role: 'user', text: question }];
    this.waitingForAnswer = true;

    this.policyChatService
      .ask({ employeeId: this.selectedEmployeeId, question }, this.debugMode)
      .subscribe({
        next: (response) => {
          this.messages = [
            ...this.messages,
            {
              role: 'assistant',
              text: response.answer,
              category: response.category,
              correlationId: response.correlationId
            }
          ];
          this.waitingForAnswer = false;
        },
        error: (err) => {
          this.errorMessage = err?.error?.message ?? 'Something went wrong talking to the assistant.';
          this.waitingForAnswer = false;
        }
      });
  }
}
