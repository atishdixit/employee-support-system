import { CommonModule } from '@angular/common';
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { EmployeeService } from '../../core/services/employee.service';
import { Employee } from '../../core/models/employee.model';
import { ErrorBannerComponent } from '../../shared/error-banner.component';

@Component({
  selector: 'app-employee-selector',
  standalone: true,
  imports: [CommonModule, FormsModule, ErrorBannerComponent],
  templateUrl: './employee-selector.component.html',
  styleUrl: './employee-selector.component.scss'
})
export class EmployeeSelectorComponent implements OnInit {
  employees: Employee[] = [];
  selectedEmployeeId = '';
  loadError: string | null = null;

  @Output() employeeChange = new EventEmitter<string>();

  constructor(private readonly employeeService: EmployeeService) {}

  ngOnInit(): void {
    this.employeeService.getEmployees().subscribe({
      next: (employees) => {
        this.employees = employees;
        if (employees.length > 0) {
          this.selectedEmployeeId = employees[0].id;
          this.employeeChange.emit(this.selectedEmployeeId);
        }
      },
      error: () => {
        this.loadError = 'Could not load the employee list. Is the backend running?';
      }
    });
  }

  onSelectionChange(): void {
    this.employeeChange.emit(this.selectedEmployeeId);
  }
}
