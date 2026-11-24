import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Navbar } from '../navbar/navbar';
import { Sidebar } from '../sidebar/sidebar';

@Component({
  selector: 'app-dashboard-layout',
  imports: [RouterOutlet, Sidebar, Navbar],
  templateUrl: './dashboard-layout.html',
  styleUrl: './dashboard-layout.scss',
})
export class DashboardLayout {
  collapsed = false;
  mobileOpen = false;

  toggleCollapse(): void {
    this.collapsed = !this.collapsed;
  }

  openMobile(): void {
    this.mobileOpen = true;
  }

  closeMobile(): void {
    this.mobileOpen = false;
  }
}
