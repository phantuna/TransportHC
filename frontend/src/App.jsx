import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import AuthPage from './pages/AuthPage';
import DriversPage from './pages/DriversPage';
import TrucksPage from './pages/TrucksPage';
import SchedulePage from './pages/SchedulePage';
import ReportsPage from './pages/ReportsPage';
import InventoryPage from './pages/InventoryPage';
import ExpensesPage from './pages/ExpensePage';
import MainLayout from './components/MainLayout';
import TravelPage from './pages/TravelPage';
import ExpenseTypePage from './pages/ExpenseTypePage';
import AccountPage from './pages/AccountsPage';

const ProtectedRoute = ({ children }) => {
  const token = localStorage.getItem('token');
  if (!token) {
    return <Navigate to="/" replace />;
  }
  return children;
};

// Component chờ cho các trang chưa dev
const Placeholder = ({ title }) => (
  <div className="text-center py-20 text-slate-400">
    <h2 className="text-2xl font-bold">{title}</h2>
    <p className="mt-2">Chức năng đang được phát triển...</p>
  </div>
);

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Trang Login (Public) */}
        <Route path="/" element={<AuthPage />} />

        {/* Các trang trong Dashboard (Protected) */}
        <Route path="/" element={<ProtectedRoute><MainLayout /></ProtectedRoute>}>
          <Route path="dashboard" element={<Placeholder title="Dashboard Thống Kê" />} />

          {/* Menu Chính */}
          <Route path="inventory" element={<InventoryPage />} />
          <Route path="expenses" element={<ExpensesPage />} />
          <Route path="travels" element={<TravelPage />} />
          <Route path="reports" element={<ReportsPage />} />

          {/* Submenu Cấu hình */}
          <Route path="trucks" element={<TrucksPage />} />
          <Route path="drivers" element={<DriversPage />} />
          <Route path="schedules" element={<SchedulePage />} />

          {/* Mấy trang này bạn chưa có code nên mình để Placeholder */}
          <Route path="accounts" element={<AccountPage />} />
          <Route path="expense-types" element={<ExpenseTypePage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;