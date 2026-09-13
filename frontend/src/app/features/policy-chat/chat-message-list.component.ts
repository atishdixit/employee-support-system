import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { ChatMessage } from '../../core/models/policy-chat.model';
import { ChatMessageComponent } from './chat-message.component';
import { LoadingSpinnerComponent } from '../../shared/loading-spinner.component';

@Component({
  selector: 'app-chat-message-list',
  standalone: true,
  imports: [CommonModule, ChatMessageComponent, LoadingSpinnerComponent],
  templateUrl: './chat-message-list.component.html',
  styleUrl: './chat-message-list.component.scss'
})
export class ChatMessageListComponent {
  @Input() messages: ChatMessage[] = [];
  @Input() waitingForAnswer = false;
}
