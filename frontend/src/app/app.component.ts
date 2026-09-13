import { Component } from '@angular/core';
import { PolicyChatComponent } from './features/policy-chat/policy-chat.component';
import { LoginComponent } from './features/login/login.component';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [PolicyChatComponent, LoginComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'Employee Support';

  constructor(readonly authService: AuthService) {}
}
