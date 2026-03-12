import React, { useEffect, useMemo, useState } from "react";
import {
  BarChart3,
  Calendar,
  Search,
  FileText,
  Truck,
  User,
  ShieldAlert,
  XCircle,
  Printer,
  X 
} from "lucide-react";
import { toast } from "react-toastify";
import moment from "moment";
import axiosClient from "../api/axiosClient";
import { hasPermission } from "../utils/auth";

const ReportsPage = () => {
  const [activeTab, setActiveTab] = useState("TRUCK_SUMMARY");
  const [reportData, setReportData] = useState([]);
  const [loading, setLoading] = useState(false);

  const [selectedPayroll, setSelectedPayroll] = useState(null);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);

  // === STATE CHO MODAL H3, H4 VÀ FORM ===
  const [isEditMonthModalOpen, setIsEditMonthModalOpen] = useState(false);
  const [isConfigDriverModalOpen, setIsConfigDriverModalOpen] = useState(false);
  const [salaryForm, setSalaryForm] = useState({
    name: "Lương cơ bản",
    type: "Khoản tăng",
    amount: ""
  });

  const [truckList, setTruckList] = useState([]);
  const [driverList, setDriverList] = useState([]);

  const canViewReport = hasPermission("REPORT_VIEW") || hasPermission("VIEW_REPORT") || hasPermission("ADMIN");
  const canManageReport = hasPermission("REPORT_MANAGE") || hasPermission("MANAGE_REPORT") || hasPermission("ADMIN");

  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    total: 0,
    totalPages: 1,
  });

  const [filters, setFilters] = useState({
    month: moment().month() + 1,
    year: moment().year(),
    truckId: "",
    driverId: "",
    fromDate: moment().startOf("month").format("YYYY-MM-DD"),
    toDate: moment().endOf("month").format("YYYY-MM-DD"),
  });

  const TABS = useMemo(
    () => [
      { id: "TRUCK_SUMMARY", label: "Tổng hợp Chi phí Xe", icon: <Truck size={18} /> },
      { id: "TRUCK_DETAIL", label: "Chi tiết Chi phí Xe", icon: <FileText size={18} /> },
      { id: "PAYROLL", label: "Bảng Lương", icon: <User size={18} /> },
      { id: "DRIVER_EXPENSE", label: "Chi phí theo Tài xế", icon: <User size={18} /> },
      { id: "TRAVEL_DAILY", label: "Nhật trình xe", icon: <Calendar size={18} /> },
    ],
    []
  );

  const formatCurrency = (val) =>
    new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(val || 0);

  const handleFilterChange = (e) => {
    setFilters((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSalaryFormChange = (e) => {
    setSalaryForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const resetData = () => {
    setReportData([]);
    setPagination({ page: 0, size: 10, total: 0, totalPages: 1 });
  };

  const handleSaveSalaryItem = async () => {
    if (!salaryForm.amount) {
      toast.warning("Vui lòng nhập số tiền!");
      return;
    }

    setLoading(true);
    try {
      if (salaryForm.name === "Lương cơ bản") {
        const res = await axiosClient.put(
          `/report/payroll/${selectedPayroll.driverId}`, 
          null, 
          {
            params: {
              month: filters.month,       
              year: filters.year,         
              amount: Number(salaryForm.amount) 
            }
          }
        );

        toast.success("Đã cập nhật Lương cơ bản thành công!");
        if (res.data) {
           setSelectedPayroll(res.data);
        }
        fetchReport(pagination.page);

      } else {
        toast.info(`Tính năng lưu khoản ${salaryForm.name} đang được phát triển...`);
      }
      setSalaryForm({ name: "Lương cơ bản", type: "Khoản tăng", amount: "" });

    } catch (error) {
      console.error("Lỗi cập nhật lương:", error);
      toast.error("Có lỗi xảy ra khi lưu cấu hình lương!");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!canViewReport || !canManageReport) return;

    const fetchDropdownData = async () => {
      try {
        const truckRes = await axiosClient.get("/truck/getAll", { params: { page: 0, size: 1000 } });
        setTruckList(truckRes.data?.content || truckRes.data?.result || []);

        const driverRes = await axiosClient.get("/user/getAll", {
          params: { page: 0, size: 1000 }
        });

        setDriverList(driverRes.data?.content || []);
      } catch (error) {
        console.error("Lỗi lấy dữ liệu dropdown", error);
      }
    };

    fetchDropdownData();
  }, [canViewReport, canManageReport]);

  const fetchReport = async (page = 0) => {
    if (!canViewReport) return;

    setLoading(true);
    setReportData([]);

    try {
      let url = "";
      let params = { page, size: pagination.size };
      const currentUserId = localStorage.getItem("userId") || localStorage.getItem("id");

      switch (activeTab) {
        case "TRUCK_SUMMARY":
          url = `/report/allTruckExpense`;
          break;

        case "TRUCK_DETAIL":
          if (canManageReport && !filters.truckId) {
            toast.warning("Vui lòng chọn Xe tải");
            setLoading(false);
            return;
          }
          const targetTruckIdDetail = canManageReport ? filters.truckId : (filters.truckId || "TODO_GET_DRIVER_TRUCK");

          if (!targetTruckIdDetail || targetTruckIdDetail === "TODO_GET_DRIVER_TRUCK") {
            toast.warning("Bạn cần chọn xe tải để xem");
            setLoading(false);
            return;
          }

          url = `/report/truckExpenseDetail/${targetTruckIdDetail}`;
          params = { ...params, from: filters.fromDate, to: filters.toDate };
          break;

        case "DRIVER_EXPENSE":
          const targetDriverId = canManageReport ? filters.driverId : currentUserId;
          if (!targetDriverId) {
            toast.warning(canManageReport ? "Vui lòng chọn Tài xế" : "Lỗi: Không tìm thấy ID tài khoản của bạn");
            setLoading(false);
            return;
          }
          url = `/report/driver-expense`;
          params = { driverId: targetDriverId, month: filters.month, year: filters.year };
          break;

        case "TRAVEL_DAILY":
          const targetTruckIdDaily = canManageReport ? filters.truckId : (filters.truckId || "TODO_GET_DRIVER_TRUCK");
          if (!targetTruckIdDaily || targetTruckIdDaily === "TODO_GET_DRIVER_TRUCK") {
            toast.warning("Bạn cần chọn xe tải để xem");
            setLoading(false);
            return;
          }
          url = `/report/travel-daily`;
          params = { truckId: targetTruckIdDaily, month: filters.month, year: filters.year };
          break;

        case "PAYROLL":
          url = `/report/payroll/all`;
          params = { ...params, month: filters.month, year: filters.year };
          break;

        default:
          setLoading(false);
          return;
      }

      const res = await axiosClient.get(url, { params });
      const data = res.data;

      if (activeTab === "DRIVER_EXPENSE" || activeTab === "TRAVEL_DAILY") {
        const items = data?.items ?? [];
        setReportData(items);
        setPagination({ page: 0, size: 10, total: items.length, totalPages: 1 });
        return;
      }

      const content = data?.content ?? data?.result?.content ?? [];
      setReportData(content);

      setPagination({
        page: data?.page ?? data?.number ?? page,
        size: data?.size ?? pagination.size,
        total: data?.totalElements ?? 0,
        totalPages: data?.totalPages ?? 1,
      });
    } catch (err) {
      console.error(err);
      toast.error("Lỗi tải báo cáo. Vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < pagination.totalPages) fetchReport(newPage);
  };

  useEffect(() => {
    resetData();
    if (activeTab === "TRUCK_SUMMARY") fetchReport(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTab]);

  const travelDailyRouteText = (item) => {
    const start = item?.startPlace ?? "";
    const end = item?.endPlace ?? "";
    if (!start && !end) return "";
    return `${start} → ${end}${item?.ganMooc ? " (gắn mooc)" : ""}`;
  };

  const renderPageNumbers = () => {
    const pages = [];
    let start = Math.max(0, pagination.page - 2);
    let end = Math.min(pagination.totalPages - 1, pagination.page + 2);

    for (let i = start; i <= end; i++) {
      pages.push(
        <button key={i} onClick={() => handlePageChange(i)}
          className={`w-8 h-8 rounded-md text-sm font-medium transition ${pagination.page === i ? "bg-blue-600 text-white" : "bg-white border hover:bg-slate-50"}`}
        >
          {i + 1}
        </button>
      );
    }
    return pages;
  };

  if (!canViewReport) {
    return (
      <div className="flex flex-col items-center justify-center h-[500px] bg-white rounded-xl shadow-sm border border-slate-200">
        <ShieldAlert size={64} className="text-amber-500 mb-4" />
        <h2 className="text-2xl font-bold text-slate-800">Không có quyền truy cập</h2>
        <p className="text-slate-500 mt-2">Tài khoản của bạn không được cấp quyền để xem Báo cáo.</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow p-6 min-h-[600px] flex flex-col relative">
      <div className="mb-6 border-b pb-4">
        <h1 className="text-2xl font-bold text-slate-800 flex items-center mb-4">
          <BarChart3 className="mr-2" /> Báo cáo Thống kê
        </h1>

        <div className="flex flex-wrap gap-2">
          {TABS.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center px-4 py-2 rounded-lg text-sm font-medium transition ${activeTab === tab.id
                ? "bg-blue-600 text-white shadow-md"
                : "bg-slate-100 text-slate-600 hover:bg-slate-200"
                }`}
            >
              <span className="mr-2">{tab.icon}</span>
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      <div className="bg-slate-50 p-4 rounded-lg border border-slate-200 mb-6 grid grid-cols-1 md:grid-cols-4 gap-4 items-end">
        {["PAYROLL", "DRIVER_EXPENSE", "TRAVEL_DAILY"].includes(activeTab) && (
          <>
            <div>
              <label className="block text-xs font-semibold text-slate-500 mb-1">Tháng</label>
              <select name="month" value={filters.month} onChange={handleFilterChange} className="w-full px-3 py-2 border rounded">
                {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => (
                  <option key={m} value={m}>Tháng {m}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-500 mb-1">Năm</label>
              <input type="number" name="year" value={filters.year} onChange={handleFilterChange} className="w-full px-3 py-2 border rounded" />
            </div>
          </>
        )}

        {activeTab === "TRUCK_DETAIL" && (
          <>
            <div>
              <label className="block text-xs font-semibold text-slate-500 mb-1">Từ ngày</label>
              <input type="date" name="fromDate" value={filters.fromDate} onChange={handleFilterChange} className="w-full px-3 py-2 border rounded" />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-500 mb-1">Đến ngày</label>
              <input type="date" name="toDate" value={filters.toDate} onChange={handleFilterChange} className="w-full px-3 py-2 border rounded" />
            </div>
          </>
        )}

        {["TRUCK_DETAIL", "TRAVEL_DAILY"].includes(activeTab) && (
          <div className="md:col-span-1">
            {canManageReport ? (
              <>
                <label className="block text-xs font-semibold text-slate-500 mb-1">Chọn xe tải</label>
                <select
                  name="truckId"
                  value={filters.truckId}
                  onChange={handleFilterChange}
                  className="w-full px-3 py-2 border rounded cursor-pointer outline-none"
                >
                  <option value="">-- Chọn biển số xe --</option>
                  {truckList.map((truck) => (
                    <option key={truck.id} value={truck.id}>
                      {truck.licensePlate} {truck.typeTruck ? `(${truck.typeTruck})` : ""}
                    </option>
                  ))}
                </select>
              </>
            ) : (
              <>
                <label className="block text-xs font-semibold text-slate-500 mb-1">Mã xe tải (Truck ID)</label>
                <input
                  type="text"
                  name="truckId"
                  placeholder="Nhập UUID xe tải..."
                  value={filters.truckId}
                  onChange={handleFilterChange}
                  className="w-full px-3 py-2 border rounded"
                />
              </>
            )}
          </div>
        )}
        {activeTab === "DRIVER_EXPENSE" && canManageReport && (
          <div className="md:col-span-1">
            <label className="block text-xs font-semibold text-slate-500 mb-1">Chọn Tài xế</label>
            <select
              name="driverId"
              value={filters.driverId}
              onChange={handleFilterChange}
              className="w-full px-3 py-2 border rounded cursor-pointer outline-none"
            >
              <option value="">-- Chọn tên tài xế --</option>
              {driverList.map((driver) => (
                <option key={driver.id} value={driver.id}>
                  {driver.username} {driver.phone ? `- ${driver.phone}` : ""}
                </option>
              ))}
            </select>
          </div>
        )}

        <div className="flex gap-2">
          <button onClick={() => fetchReport(0)} className="flex items-center px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 font-medium">
            <Search size={16} className="mr-2" /> Xem Báo cáo
          </button>
        </div>
      </div>

      <div className="flex-1 overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-slate-50 text-slate-500 border-b text-xs uppercase">
              <th className="px-4 py-3">STT</th>
              {activeTab === "TRUCK_SUMMARY" && (
                <>
                  <th className="px-4 py-3">Biển số xe</th>
                  <th className="px-4 py-3 text-right">Tổng chi phí</th>
                </>)}
              {activeTab === "TRUCK_DETAIL" && (
                <>
                  <th className="px-4 py-3">Ngày</th>
                  <th className="px-4 py-3">Loại chi phí</th>
                  <th className="px-4 py-3">Mô tả</th>
                  <th className="px-4 py-3 text-right">Số tiền</th>
                </>)}
              {activeTab === "PAYROLL" && (
                <>
                  <th className="px-4 py-3">Mã NV</th>
                  <th className="px-4 py-3">Họ tên</th>
                  <th className="px-4 py-3 text-right">Lương chuyến</th>
                  <th className="px-4 py-3 text-right">Lương khác</th>
                  <th className="px-4 py-3 text-right">Khoản trừ</th>
                  <th className="px-4 py-3 text-right">Thực nhận</th>
                  <th className="px-4 py-3 text-center">Action</th>
                </>)}
              {activeTab === "DRIVER_EXPENSE" && (
                <>
                  <th className="px-4 py-3">Loại chi phí</th>
                  <th className="px-4 py-3 text-right">Số tiền</th>
                </>)}
              {activeTab === "TRAVEL_DAILY" && (
                <>
                  <th className="px-4 py-3">Ngày</th>
                  <th className="px-4 py-3 text-right">Số chuyến</th>
                  <th className="px-4 py-3">Lộ trình</th>
                </>)}
            </tr>
          </thead>

          <tbody className="text-sm text-slate-700 divide-y">
            {loading ? (
              <tr><td colSpan="10" className="text-center py-10">Đang tải dữ liệu...</td></tr>
            ) : reportData.length === 0 ? (
              <tr><td colSpan="10" className="text-center py-10 text-slate-400">Không có dữ liệu</td></tr>
            ) : (
              reportData.map((item, index) => (
                <tr key={index} className="hover:bg-slate-50">
                  <td className="px-4 py-3 text-center w-12">{index + 1 + pagination.page * pagination.size}</td>
                  {activeTab === "TRUCK_SUMMARY" && (
                    <>
                      <td className="px-4 py-3 font-bold">{item.licensePlate}</td>
                      <td className="px-4 py-3 text-right font-mono text-red-600">{formatCurrency(item.totalExpense)}</td>
                    </>
                  )}
                  {activeTab === "TRUCK_DETAIL" && (
                    <>
                      <td className="px-4 py-3">{item.date ?? item.expenseDate ?? item.createdAt ?? item.createdDate ?? "-"}</td>
                      <td className="px-4 py-3"><span className="bg-slate-100 px-2 py-1 rounded text-xs">{item.type}</span></td>
                      <td className="px-4 py-3">{item.description ?? "-"}</td>
                      <td className="px-4 py-3 text-right font-mono">{formatCurrency(item.amount ?? item.totalAmount ?? item.expense ?? 0)}</td>
                    </>)}
                  {activeTab === "PAYROLL" && (
                    <>
                      <td className="px-4 py-3 font-medium text-slate-600">{item.driverId}</td>
                      <td className="px-4 py-3 font-bold text-slate-800">{item.name}</td>
                      <td className="px-4 py-3 text-right font-mono text-blue-600">
                        {formatCurrency(item.totalTripSalary)}
                      </td>
                      <td className="px-4 py-3 text-right font-mono">
                        {formatCurrency(item.totalIncome - item.totalTripSalary)}
                      </td>
                      <td className="px-4 py-3 text-right font-mono text-rose-500">
                        -{formatCurrency(item.totalDeduction)}
                      </td>
                      <td className="px-4 py-3 text-right font-bold text-emerald-600">
                        {formatCurrency(item.totalSalary)}
                      </td>
                      <td className="px-4 py-3 text-center">
                        <div className="flex justify-center gap-2">
                          <button onClick={() => { setSelectedPayroll(item); setIsDetailModalOpen(true); }} className="p-1 hover:text-blue-600 cursor-pointer">
                            <FileText size={16} />
                          </button>
                        </div>
                      </td>
                    </>)}
                  {activeTab === "DRIVER_EXPENSE" && (
                    <>
                      <td className="px-4 py-3 font-medium">{item.type}</td>
                      <td className="px-4 py-3 text-right font-mono">{formatCurrency(item.amount)}</td>
                    </>)}
                  {activeTab === "TRAVEL_DAILY" && (
                    <>
                      <td className="px-4 py-3">{item.date}</td>
                      <td className="px-4 py-3 text-right font-bold">{item.tripCount}</td>
                      <td className="px-4 py-3 text-xs text-slate-600">{travelDailyRouteText(item)}</td>
                    </>)}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {pagination.totalPages > 1 && (
        <div className="flex justify-between items-center mt-4 pt-4 border-t">
          <span className="text-sm text-slate-500">Trang {pagination.page + 1} / {pagination.totalPages}</span>
          <div className="flex items-center gap-2">
            <button onClick={() => handlePageChange(pagination.page - 1)} disabled={pagination.page === 0} className="px-3 py-1 border rounded hover:bg-slate-50 disabled:opacity-50">Trước</button>
            <div className="hidden sm:flex gap-1">{renderPageNumbers()}</div>
            <button onClick={() => handlePageChange(pagination.page + 1)} disabled={pagination.page >= pagination.totalPages - 1} className="px-3 py-1 border rounded hover:bg-slate-50 disabled:opacity-50">Sau</button>
          </div>
        </div>
      )}

      {/* =========================================
          MODAL H2: BÁO CÁO LƯƠNG CHI TIẾT
      ========================================= */}
      {isDetailModalOpen && selectedPayroll && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center bg-black/50 p-4 overflow-y-auto">
          <div className="bg-white rounded w-full max-w-6xl p-6 shadow-2xl">
            {/* Header Modal H2 */}
            <div className="flex justify-between items-start mb-6 border-b pb-4">
              <div>
                <h2 className="text-xl font-bold text-slate-800">Báo cáo tiền lương</h2>
                <p className="text-sm text-slate-600 mt-2">Báo cáo lương tháng {filters.month}/{filters.year}</p>
                <p className="text-sm font-semibold">{selectedPayroll.driverId || "NV203243"} - {selectedPayroll.name}</p>
              </div>
              <div className="flex gap-2">
                <button onClick={() => { setIsDetailModalOpen(false); setIsEditMonthModalOpen(true); }} className="px-4 py-2 bg-blue-500 text-white rounded text-sm font-medium hover:bg-blue-600">Chỉnh sửa tháng này</button>
                <button onClick={() => { setIsDetailModalOpen(false); setIsConfigDriverModalOpen(true); }} className="px-4 py-2 bg-blue-500 text-white rounded text-sm font-medium hover:bg-blue-600">Cấu hình tài xế</button>
                <button className="px-4 py-2 bg-blue-500 text-white rounded text-sm font-medium hover:bg-blue-600">Export</button>
                <button onClick={() => setIsDetailModalOpen(false)} className="ml-4 text-slate-400 hover:text-rose-500 transition"><XCircle size={28} /></button>
              </div>
            </div>

            {/* Bảng H2: Thu nhập & Khấu trừ nằm ngang nhau */}
            <div className="grid grid-cols-2 gap-4 mb-4">
              {/* Cột 1: Thu nhập */}
              <table className="w-full text-xs border border-slate-300">
                <thead className="bg-yellow-100 text-slate-800">
                  <tr><th className="border p-2 w-8 text-center">A</th><th className="border p-2">Các Khoản Thu Nhập</th><th className="border p-2 text-right">Cơ bản</th><th className="border p-2 text-right">Thực lãnh</th></tr>
                </thead>
                <tbody>
                  {selectedPayroll.incomes?.map((inc, i) => (
                    <tr key={i}><td className="border p-2 text-center">{i + 1}</td><td className="border p-2 font-medium">{inc.name}</td><td className="border p-2 text-right">{formatCurrency(inc.actualAmount)}</td><td className="border p-2 text-right font-bold">{formatCurrency(inc.actualAmount)}</td></tr>
                  ))}
                  {(!selectedPayroll.incomes || selectedPayroll.incomes.length === 0) && (
                    <tr><td colSpan="4" className="border p-2 text-center text-slate-400">Không có dữ liệu thu nhập khác</td></tr>
                  )}
                </tbody>
              </table>

              {/* Cột 2: Khấu trừ */}
              <table className="w-full text-xs border border-slate-300">
                <thead className="bg-yellow-100 text-slate-800">
                  <tr><th className="border p-2 w-8 text-center">STT</th><th className="border p-2">Các Khoản Trừ Vào Lương</th><th className="border p-2 text-right">Số Tiền</th></tr>
                </thead>
                <tbody>
                  {selectedPayroll.deductions?.map((ded, i) => (
                    <tr key={i}><td className="border p-2 text-center">{i + 1}</td><td className="border p-2 font-medium">{ded.name}</td><td className="border p-2 text-right font-bold">{formatCurrency(ded.amount)}</td></tr>
                  ))}
                  {(!selectedPayroll.deductions || selectedPayroll.deductions.length === 0) && (
                    <tr><td colSpan="3" className="border p-2 text-center text-slate-400">Không có dữ liệu khấu trừ</td></tr>
                  )}
                </tbody>
              </table>
            </div>

            {/* Bảng H2: Lương chuyến */}
            <table className="w-full text-xs border border-slate-300 mb-6">
              <thead className="bg-yellow-100 text-slate-800">
                <tr><th className="border p-2 w-8 text-center">I</th><th className="border p-2 text-left">Lương chuyến vận chuyển</th><th className="border p-2 text-center">Số chuyến</th><th className="border p-2 text-right">Đơn giá</th><th className="border p-2 text-right">Thành tiền</th></tr>
              </thead>
              <tbody>
                {selectedPayroll.trips?.map((trip, i) => (
                  <tr key={i}>
                    <td className="border p-2 text-center">{i + 1}</td>
                    <td className="border p-2">{trip.routeName}</td>
                    <td className="border p-2 text-center">{trip.tripCount}</td>
                    <td className="border p-2 text-right">{formatCurrency(trip.unitPrice)}</td>
                    <td className="border p-2 text-right font-bold">{formatCurrency(trip.amount)}</td>
                  </tr>
                ))}
                {(!selectedPayroll.trips || selectedPayroll.trips.length === 0) && (
                  <tr><td colSpan="5" className="border p-2 text-center text-slate-400">Không có dữ liệu chuyến đi</td></tr>
                )}
                <tr className="bg-slate-50 font-bold">
                  <td colSpan="2" className="border p-2 text-center">Tổng Cộng Lương Chuyến</td>
                  <td className="border p-2 text-center"></td>
                  <td className="border p-2 text-right"></td>
                  <td className="border p-2 text-right text-blue-600">{formatCurrency(selectedPayroll.totalTripSalary)}</td>
                </tr>
              </tbody>
            </table>

            <div>
              <p className="text-sm font-medium text-slate-600">Số tiền lương thực nhận: <span className="text-red-600 font-bold text-lg">{formatCurrency(selectedPayroll.totalSalary)}</span></p>
            </div>
          </div>
        </div>
      )}

      {/* =========================================
          MODAL H3 & H4: CẤU HÌNH / CHỈNH SỬA LƯƠNG
      ========================================= */}
      {(isEditMonthModalOpen || isConfigDriverModalOpen) && selectedPayroll && (
        <div className="fixed inset-0 z-[120] flex items-center justify-center bg-black/50 p-4">
          <div className="bg-slate-50 rounded-lg w-full max-w-4xl shadow-2xl overflow-hidden flex flex-col h-[80vh]">
            <div className="bg-white p-6 border-b flex justify-between items-center">
              <div>
                <h2 className="text-xl font-bold text-slate-800">
                  {isEditMonthModalOpen ? "Lương tài xế (theo tháng)" : "Cấu hình lương tài xế"}
                </h2>
                <div className="flex gap-4 mt-2 text-sm text-slate-600">
                  <p>Mã NV: {selectedPayroll.driverId || "23434"}</p>
                  <p>Tài xế: {selectedPayroll.name}</p>
                  {isEditMonthModalOpen && <p>Tháng: {filters.month}/{filters.year}</p>}
                </div>
              </div>
              <div className="flex gap-2">
                <button className="px-4 py-2 bg-blue-500 text-white rounded text-sm hover:bg-blue-600">Export</button>
                <button onClick={() => { setIsEditMonthModalOpen(false); setIsConfigDriverModalOpen(false); setIsDetailModalOpen(true); }} className="p-2 text-slate-400 hover:text-slate-600"><X size={24} /></button>
              </div>
            </div>

            <div className="p-6 overflow-y-auto flex-1 relative">
              {/* Bảng danh sách cấu hình hiện tại */}
              <table className="w-full text-left text-sm mb-8 bg-white shadow-sm rounded">
                <thead className="border-b bg-slate-50">
                  <tr><th className="p-4 w-12">#</th><th className="p-4">Mã khoản</th><th className="p-4">Tên khoản</th><th className="p-4">Số tiền</th><th className="p-4">Loại</th><th className="p-4 text-center">Action</th></tr>
                </thead>
                <tbody className="divide-y">
                  {/* Mockup Data (bạn có thể thay bằng vòng lặp data thật sau) */}
                  <tr><td className="p-4">1</td><td className="p-4 text-slate-500">001</td><td className="p-4 font-medium">Lương cơ bản</td><td className="p-4">6,233,000</td><td className="p-4">Khoản tăng</td><td className="p-4 text-center"><button className="text-slate-400 hover:text-red-500"><X size={18} /></button></td></tr>
                  <tr><td className="p-4">2</td><td className="p-4 text-slate-500">002</td><td className="p-4 font-medium">Phụ cấp điện thoại</td><td className="p-4">200,000</td><td className="p-4">Khoản tăng</td><td className="p-4 text-center"><button className="text-slate-400 hover:text-red-500"><X size={18} /></button></td></tr>
                  <tr><td className="p-4">3</td><td className="p-4 text-slate-500">003</td><td className="p-4 font-medium">Phụ cấp công việc</td><td className="p-4">3,450,000</td><td className="p-4">Khoản giảm</td><td className="p-4 text-center"><button className="text-slate-400 hover:text-red-500"><X size={18} /></button></td></tr>
                </tbody>
              </table>

              {/* Form thêm mới khoản */}
              <div className="max-w-md mx-auto bg-white p-8 rounded shadow border">
                <div className="flex flex-col gap-4">
                  <div className="flex items-center">
                    <label className="w-24 text-sm text-slate-600">Tên khoản</label>
                    <select name="name" value={salaryForm.name} onChange={handleSalaryFormChange} className="flex-1 border p-2 rounded text-sm bg-slate-50 outline-none">
                      <option>Lương cơ bản</option>
                      <option>Phụ cấp điện thoại</option>
                      <option>Thưởng doanh số</option>
                    </select>
                  </div>
                  {/* Hiển thị dropdown Loại nếu ở màn hình cấu hình chung, hoặc có thể hiển thị luôn cũng được */}
                  <div className="flex items-center">
                    <label className="w-24 text-sm text-slate-600">Loại</label>
                    <select name="type" value={salaryForm.type} onChange={handleSalaryFormChange} className="flex-1 border p-2 rounded text-sm bg-slate-50 outline-none">
                      <option>Khoản tăng</option>
                      <option>Khoản giảm</option>
                    </select>
                  </div>
                  <div className="flex items-center">
                    <label className="w-24 text-sm text-slate-600">Số tiền</label>
                    <input type="number" name="amount" value={salaryForm.amount} onChange={handleSalaryFormChange} className="flex-1 border p-2 rounded text-sm outline-none" placeholder="Nhập số tiền..." />
                  </div>
                  <div className="flex justify-center mt-6">
                    <button onClick={handleSaveSalaryItem} className="px-8 py-2 bg-blue-500 text-white rounded font-medium hover:bg-blue-600">Lưu lại</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ReportsPage;