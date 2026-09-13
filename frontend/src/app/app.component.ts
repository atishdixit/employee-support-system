import { Component } from '@angular/core';
import { PolicyChatComponent } from './features/policy-chat/policy-chat.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [PolicyChatComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'Employee Support';
}
