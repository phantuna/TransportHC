import React, { useEffect, useState, useCallback } from "react";
import {
  Edit, Trash2, Plus, ChevronLeft, ChevronRight, Route, XCircle, Save, Calendar, Truck, AlertTriangle
} from "lucide-react";
import { toast } from "react-toastify";
import moment from "moment";
import axiosClient from "../api/axiosClient";
import { hasPermission } from "../utils/auth";

const TravelPage = () => {
  const [travels, setTravels] = useState([]);
  const [loading, setLoading] = useState(true);

  // Dữ liệu cho Dropdown
  const [truckList, setTruckList] = useState([]);
  const [scheduleList, setScheduleList] = useState([]);

  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    total: 0,
    totalPages: 1,
  });

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState("create");
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [itemToDelete, setItemToDelete] = useState(null);

  const [formData, setFormData] = useState({
    id: "",
    truckId: "",
    scheduleId: "",
    startDate: moment().format("YYYY-MM-DD"),
    endDate: moment().add(1, 'days').format("YYYY-MM-DD"),
  });

  // Check quyền (Dựa theo Backend: TRAVEL)
  const canManage = hasPermission("TRAVEL_MANAGE") || hasPermission("MANAGE_TRAVEL") || hasPermission("ADMIN");
  const canView = hasPermission("TRAVEL_VIEW") || hasPermission("VIEW_TRAVEL") || canManage;

  // --- HÀM FORMAT TIỀN TỆ ---
  const formatCurrency = (amount) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(amount || 0);
  };

  // --- 1. FETCH DATA ---
  const fetchTravels = useCallback(async (page = 0) => {
    if (!canView) return;
    setLoading(true);
    try {
      const res = await axiosClient.get("/travel/getAll", {
        params: { page, size: pagination.size },
      });
      const data = res.data?.result || res.data;
      
      setTravels(data.content || []);
      setPagination(prev => ({
        ...prev,
        page: data.page ?? data.number ?? page,
        total: data.totalElements ?? 0,
        totalPages: data.totalPages ?? 1,
      }));
    } catch (error) {
      toast.error("Không thể tải danh sách chuyến đi!");
    } finally {
      setLoading(false);
    }
  }, [canView, pagination.size]);

  // --- 2. LẤY DROPDOWN XE VÀ LỊCH TRÌNH ---
  const fetchDropdownData = async () => {
    if (!canManage) return;
    try {
      // Lấy tất cả xe
      const truckRes = await axiosClient.get("/truck/getAll", { params: { page: 0, size: 1000 } });
      setTruckList(truckRes.data?.content || truckRes.data?.result || []);

      // Lấy tất cả lịch trình mẫu
      const scheduleRes = await axiosClient.get("/schedule/getAll", { params: { page: 0, size: 1000 } });
      setScheduleList(scheduleRes.data?.content || scheduleRes.data?.result || []);
    } catch (error) {
      console.error("Lỗi tải dữ liệu dropdown", error);
    }
  };

  useEffect(() => {
    fetchTravels(0);
    fetchDropdownData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [fetchTravels]);

  // --- 3. TẠO & CẬP NHẬT ---
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (moment(formData.endDate).isBefore(moment(formData.startDate))) {
      return toast.warning("Ngày kết thúc không được nhỏ hơn ngày bắt đầu!");
    }

    const payload = {
      truckId: formData.truckId,
      scheduleId: formData.scheduleId,
      startDate: formData.startDate,
      endDate: formData.endDate,
    };

    try {
      if (modalMode === "create") {
        await axiosClient.post("/travel/created", payload);
        toast.success("Tạo hành trình thành công!");
      } else {
        await axiosClient.put(`/travel/updated/${formData.id}`, payload);
        toast.success("Cập nhật hành trình thành công!");
      }
      setIsModalOpen(false);
      fetchTravels(modalMode === "create" ? 0 : pagination.page);
    } catch (error) {
      toast.error(error?.response?.data?.message || "Thao tác thất bại!");
    }
  };

  // --- 4. XÓA ---
  const confirmDelete = async () => {
    if (!itemToDelete) return;
    try {
      await axiosClient.delete(`/travel/deleted/${itemToDelete}`);
      toast.success("Đã xóa hành trình!");
      setIsDeleteModalOpen(false);
      setItemToDelete(null);
      fetchTravels(pagination.page);
    } catch (error) {
      toast.error(error?.response?.data?.message || "Xóa thất bại!");
    }
  };

  // --- HÀM UI ---
  const openCreateModal = () => {
    setModalMode("create");
    setFormData({ 
      id: "", truckId: "", scheduleId: "", 
      startDate: moment().format("YYYY-MM-DD"), 
      endDate: moment().add(1, 'days').format("YYYY-MM-DD") 
    });
    setIsModalOpen(true);
  };

  const openEditModal = (item) => {
    setModalMode("edit");

    // XỬ LÝ LỖI TRỐNG DROPDOWN XE TẢI KHI SỬA
    let matchedTruckId = item.truckId;
    if (!matchedTruckId && item.truckPlate) {
      const found = truckList.find(t => t.licensePlate === item.truckPlate);
      if (found) matchedTruckId = found.id;
    }

    setFormData({
      id: item.travelId || item.id,
      truckId: matchedTruckId || "",
      scheduleId: item.scheduleId || "",
      startDate: item.startDate ? moment(item.startDate).format("YYYY-MM-DD") : "",
      endDate: item.endDate ? moment(item.endDate).format("YYYY-MM-DD") : "",
    });
    setIsModalOpen(true);
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < pagination.totalPages) fetchTravels(newPage);
  };

  const renderPageNumbers = () => {
    const pages = [];
    let startPage = Math.max(0, pagination.page - 2);
    let endPage = Math.min(pagination.totalPages - 1, pagination.page + 2);
    for (let i = startPage; i <= endPage; i++) {
      pages.push(
        <button key={i} onClick={() => handlePageChange(i)}
          className={`w-8 h-8 rounded-md text-sm font-medium transition ${pagination.page === i ? "bg-blue-600 text-white" : "bg-white border hover:bg-slate-50 text-slate-600"}`}
        >
          {i + 1}
        </button>
      );
    }
    return pages;
  };

  if (!canView) {
    return (
      <div className="flex flex-col items-center justify-center h-64 bg-white rounded-xl shadow-sm border border-slate-200">
        <AlertTriangle size={48} className="text-amber-500 mb-3" />
        <h2 className="text-xl font-bold text-slate-800">Truy cập bị từ chối</h2>
        <p className="text-slate-500 mt-1">Bạn không có quyền xem Danh sách hành trình.</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl shadow-sm p-6 min-h-[600px] flex flex-col justify-between border border-slate-200">
      <div>
        <div className="flex flex-col md:flex-row justify-between items-center mb-6 gap-4">
          <div>
            <h1 className="text-2xl font-extrabold text-slate-800 flex items-center tracking-tight">
              <div className="p-2 bg-blue-600 rounded-lg mr-3 shadow-md shadow-blue-200">
                <Route size={24} className="text-white" />
              </div>
              Danh sách Hành trình
            </h1>
            <p className="text-sm text-slate-500 mt-2 ml-14 font-medium">
              Đang quản lý <span className="font-bold text-blue-600">{pagination.total}</span> hành trình
            </p>
          </div>
          {canManage && (
            <button onClick={openCreateModal} className="flex items-center px-4 py-2.5 bg-blue-600 text-white rounded-xl hover:bg-blue-700 text-sm font-bold transition shadow-lg shadow-blue-200">
              <Plus size={18} className="mr-2" /> Thêm chuyến đi
            </button>
          )}
        </div>

        <div className="overflow-x-auto border border-slate-200 rounded-xl">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50/80 text-slate-500 text-[11px] uppercase tracking-wider border-b border-slate-200">
                <th className="px-6 py-4 font-bold">STT</th>
                <th className="px-6 py-4 font-bold">Biển số xe</th>
                <th className="px-6 py-4 font-bold">Tài xế</th>
                <th className="px-6 py-4 font-bold">Lịch trình (Tuyến)</th>
                <th className="px-6 py-4 font-bold">Ngày đi</th>
                <th className="px-6 py-4 font-bold">Ngày về</th>
                <th className="px-6 py-4 font-bold">Chi phí</th> 
                {canManage && <th className="px-6 py-4 font-bold text-right">Thao tác</th>}
              </tr>
            </thead>
            <tbody className="text-sm text-slate-700 divide-y divide-slate-100">
              {loading ? (
                <tr><td colSpan="8" className="text-center py-16 text-slate-400 animate-pulse">Đang tải dữ liệu...</td></tr>
              ) : travels.length === 0 ? (
                <tr><td colSpan="8" className="text-center py-16 text-slate-400">Chưa có hành trình nào</td></tr>
              ) : (
                travels.map((item, index) => (
                  <tr key={item.travelId || item.id} className="hover:bg-blue-50/40 transition-colors group">
                    <td className="px-6 py-4 text-slate-400 font-medium">
                      {((pagination.page || 0) * (pagination.size || 10)) + index + 1}
                    </td>
                    <td className="px-6 py-4 font-bold text-slate-800 uppercase">{item.licensePlate || item.truckPlate || "N/A"}</td>

                    <td className="px-6 py-4 font-medium text-slate-700">
                      {item.driverName ? item.driverName : <span className="text-slate-400 italic">Chưa có</span>}
                    </td>

                    <td className="px-6 py-4 text-blue-600 font-medium">
                      {item.scheduleName || (item.startPlace && item.endPlace ? `${item.startPlace} - ${item.endPlace}` : "Chưa xác định")}
                    </td>
                    <td className="px-6 py-4">{item.startDate ? moment(item.startDate).format('DD/MM/YYYY') : '-'}</td>
                    <td className="px-6 py-4 text-slate-500">{item.endDate ? moment(item.endDate).format('DD/MM/YYYY') : '-'}</td>
                    <td className="px-6 py-4 font-mono font-semibold text-rose-600">
                      {formatCurrency(item.totalExpense)}
                    </td>

                    {canManage && (
                      <td className="px-6 py-4 text-right">
                        <div className="flex items-center justify-end gap-1 transition-opacity">
                          <button onClick={() => openEditModal(item)} className="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg">
                            <Edit size={16} />
                          </button>
                          <button onClick={() => { setItemToDelete(item.travelId || item.id); setIsDeleteModalOpen(true); }} className="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg">
                            <Trash2 size={16} />
                          </button>
                        </div>
                      </td>
                    )}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* PHÂN TRANG */}
      {pagination.totalPages > 1 && (
        <div className="flex justify-between items-center mt-6 pt-4 border-t border-slate-100 gap-4">
          <div className="text-sm text-slate-500 font-medium">
            Trang {pagination.page + 1} / {pagination.totalPages}
          </div>
          <div className="flex items-center gap-2">
            <button onClick={() => handlePageChange(pagination.page - 1)} disabled={pagination.page === 0} className="p-1.5 border rounded-lg hover:bg-slate-50 disabled:opacity-30"><ChevronLeft size={18} /></button>
            <div className="hidden sm:flex gap-1">{renderPageNumbers()}</div>
            <button onClick={() => handlePageChange(pagination.page + 1)} disabled={pagination.page >= pagination.totalPages - 1} className="p-1.5 border rounded-lg hover:bg-slate-50 disabled:opacity-30"><ChevronRight size={18} /></button>
          </div>
        </div>
      )}

      {/* MODAL FORM */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-lg overflow-hidden animate-in zoom-in duration-200">
            <div className="bg-slate-800 px-6 py-4 flex justify-between items-center">
              <h3 className="text-white font-bold tracking-wide">
                {modalMode === 'create' ? 'PHÂN CÔNG CHUYẾN ĐI' : 'CẬP NHẬT CHUYẾN ĐI'}
              </h3>
              <button onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-white"><XCircle size={22} /></button>
            </div>

            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              <div>
                <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Chọn xe tải *</label>
                <div className="relative">
                  <select required
                    className="w-full pl-10 pr-3 py-2.5 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition text-sm"
                    value={formData.truckId} onChange={(e) => setFormData({ ...formData, truckId: e.target.value })}
                  >
                    <option value="">-- Vui lòng chọn xe --</option>
                    {truckList.map(t => (
                      <option key={t.id} value={t.id}>{t.licensePlate} {t.driverName ? `(Tài xế: ${t.driverName})` : "(Chưa có tài xế)"}</option>
                    ))}
                  </select>
                  <Truck size={16} className="absolute left-3 top-3 text-slate-400" />
                </div>
              </div>

              <div>
                <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Lịch trình mẫu *</label>
                <div className="relative">
                  <select required
                    className="w-full pl-10 pr-3 py-2.5 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition text-sm"
                    value={formData.scheduleId} onChange={(e) => setFormData({ ...formData, scheduleId: e.target.value })}
                  >
                    <option value="">-- Chọn tuyến đường --</option>
                    {scheduleList.map(s => (
                      <option key={s.id} value={s.id}>{s.startPlace} - {s.endPlace}</option>
                    ))}
                  </select>
                  <Route size={16} className="absolute left-3 top-3 text-slate-400" />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Ngày đi *</label>
                  <div className="relative">
                    <input type="date" required
                      className="w-full pl-9 pr-3 py-2.5 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none text-sm"
                      value={formData.startDate} onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
                    />
                    <Calendar size={16} className="absolute left-3 top-3 text-slate-400" />
                  </div>
                </div>
                <div>
                  <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Ngày về dự kiến *</label>
                  <div className="relative">
                    <input type="date" required
                      className="w-full pl-9 pr-3 py-2.5 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none text-sm"
                      value={formData.endDate} onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
                    />
                    <Calendar size={16} className="absolute left-3 top-3 text-slate-400" />
                  </div>
                </div>
              </div>

              <div className="pt-4 flex justify-end gap-3">
                <button type="button" onClick={() => setIsModalOpen(false)} className="px-5 py-2.5 border rounded-lg text-slate-600 font-bold text-sm hover:bg-slate-50">HỦY</button>
                <button type="submit" className="px-5 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-bold text-sm flex items-center shadow-lg">
                  <Save size={18} className="mr-2" /> LƯU LẠI
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL XÓA */}
      {isDeleteModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-sm p-6 text-center">
            <div className="w-16 h-16 bg-rose-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <AlertTriangle size={32} className="text-rose-600" />
            </div>
            <h3 className="text-xl font-bold text-slate-800 mb-2">Xác nhận xóa</h3>
            <p className="text-slate-500 text-sm mb-6">Bạn có chắc chắn muốn xóa chuyến đi này không?</p>
            <div className="flex justify-center gap-3">
              <button onClick={() => setIsDeleteModalOpen(false)} className="px-5 py-2 border rounded-lg text-slate-600 font-bold text-sm hover:bg-slate-50">HỦY</button>
              <button onClick={confirmDelete} className="px-5 py-2 bg-rose-600 text-white rounded-lg hover:bg-rose-700 font-bold text-sm shadow-lg">XÓA NGAY</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default TravelPage;