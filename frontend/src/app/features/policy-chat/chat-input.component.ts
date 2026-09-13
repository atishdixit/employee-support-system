import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-chat-input',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat-input.component.html',
  styleUrl: './chat-input.component.scss'
})
export class ChatInputComponent {
  @Input() disabled = false;
  @Output() ask = new EventEmitter<string>();

  question = '';

  submit(): void {
    const trimmed = this.question.trim();
    if (!trimmed || this.disabled) {
      return;
    }
    this.ask.emit(trimmed);
    this.question = '';
  }
}
