import React, { useEffect, useState } from "react";
import {
  Edit,
  Trash2,
  Plus,
  ChevronLeft,
  ChevronRight,
  DollarSign,
  FileText,
  CheckCircle,
  XCircle,
  Truck,
  MapPin,
  Save // ĐÃ THÊM SAVE VÀO ĐÂY ĐỂ SỬA LỖI
} from "lucide-react";
import { toast } from "react-toastify";
import moment from "moment";
import axiosClient from "../api/axiosClient";

const ExpensesPage = () => {
  const [expenses, setExpenses] = useState([]);
  const [travelList, setTravelList] = useState([]); // STATE MỚI: Lưu danh sách chuyến đi
  const [loading, setLoading] = useState(true);

  // State phân trang
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    total: 0,
    totalPages: 1,
  });

  // State Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState("create");

  const [formData, setFormData] = useState({
    id: "",
    type: "FUEL",
    expense: "",
    description: "",
    travelId: "",
    incurredDate: moment().format("YYYY-MM-DD"),
  });

  // --- API: LẤY DANH SÁCH CHI PHÍ ---
  const fetchExpenses = async (page = 0) => {
    setLoading(true);
    try {
      const res = await axiosClient.get("/expense/getAll", {
        params: { page, size: pagination.size },
      });

      const data = res.data;
      const listData = data.content || data.result || [];
      setExpenses(listData);

      setPagination({
        page: data.number ?? page,
        size: data.size ?? pagination.size,
        total: data.totalElements ?? listData.length ?? 0,
        totalPages: data.totalPages ?? 1,
      });
    } catch (error) {
      console.error(error);
      toast.error("Không thể tải danh sách chi phí (check token/quyền)");
    } finally {
      setLoading(false);
    }
  };

  // --- API MỚI: LẤY DANH SÁCH CHUYẾN ĐI (Để map ID -> Tên) ---
  const fetchTravelsForDropdown = async () => {
    try {
      const res = await axiosClient.get("/travel/getAll", {
        params: { page: 0, size: 1000 }, // Lấy nhiều để đủ data map
      });
      
      // FIX BÓC TÁCH DATA: Đảm bảo chắc chắn lấy ra một Mảng (Array)
      const responseData = res.data?.result || res.data;
      const listArray = responseData?.content || responseData || [];
      
      setTravelList(Array.isArray(listArray) ? listArray : []);
    } catch (error) {
      console.error("Lỗi lấy danh sách chuyến đi", error);
    }
  };

  // --- HÀM HELPER: DỊCH TRAVEL ID -> THÔNG TIN HIỂN THỊ ---
  const getTravelDisplayInfo = (travelId) => {
    if (!travelId) return { route: "Chưa gán", details: "" };
    
    // Tìm chuyến đi khớp ID
    const travel = travelList.find(t => (t.travelId === travelId || t.id === travelId));
    
    if (!travel) return { route: "Chuyến đi ẩn/đã xóa", details: travelId.substring(0,8) + "..." };

    const routeName = travel.scheduleName || (travel.startPlace && travel.endPlace ? `${travel.startPlace} → ${travel.endPlace}` : "Chưa xác định");
    const plate = travel.truckPlate || travel.licensePlate || "Xe ẩn";
    const date = travel.startDate ? moment(travel.startDate).format("DD/MM/YYYY") : "";

    return { route: routeName, details: `${plate} (${date})` };
  };

  // --- API: DUYỆT CHI PHÍ ---
  const handleApproval = async (id) => {
    try {
      await axiosClient.post(`/expense/approval`, null, { params: { id } });
      toast.success("Đã duyệt chi phí thành công!");
      fetchExpenses(pagination.page);
    } catch (error) {
      console.error(error);
      const msg = error?.response?.data?.message || "Duyệt thất bại!";
      toast.error(msg);
    }
  };

  // --- API: XÓA ---
  const handleDelete = async (id) => {
    if (!window.confirm("Bạn có chắc chắn muốn xóa phiếu chi này?")) return;
    try {
      await axiosClient.delete(`/expense/delete/${id}`);
      toast.success("Xóa thành công!");
      fetchExpenses(pagination.page);
    } catch (error) {
      console.error(error);
      toast.error("Xóa thất bại!");
    }
  };

  // --- API: TẠO MỚI / CẬP NHẬT ---
  const handleSubmit = async (e) => {
    e.preventDefault();

    const payload = {
      type: formData.type,
      expense: Number(formData.expense),
      description: formData.description,
      travelId: formData.travelId,
      incurredDate: formData.incurredDate,
    };

    try {
      if (modalMode === "create") {
        await axiosClient.post("/expense/created", payload);
        toast.success("Tạo phiếu chi thành công!");
      } else {
        await axiosClient.put(`/expense/updated/${formData.id}`, payload);
        toast.success("Cập nhật thành công!");
      }

      setIsModalOpen(false);
      fetchExpenses(0);
    } catch (error) {
      console.error(error);
      const msg = error?.response?.data?.message || "Có lỗi xảy ra!";
      toast.error(msg);
    }
  };

  // --- HELPER & UI LOGIC ---
  const openCreateModal = () => {
    setModalMode("create");
    setFormData({
      id: "",
      type: "FUEL",
      expense: "",
      description: "",
      travelId: "",
      incurredDate: moment().format("YYYY-MM-DD"),
    });
    setIsModalOpen(true);
  };

  const openEditModal = (item) => {
    setModalMode("edit");
    setFormData({
      id: item.id,
      type: item.type,
      expense: item.expense,
      description: item.description,
      travelId: item.travelId || "",
      incurredDate: item.incurredDate ? moment(item.incurredDate).format("YYYY-MM-DD") : "",
    });
    setIsModalOpen(true);
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < pagination.totalPages) fetchExpenses(newPage);
  };

  useEffect(() => {
    fetchExpenses(0);
    fetchTravelsForDropdown();
  }, []);

  const formatCurrency = (amount) =>
    new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(amount || 0);

  const getApprovalBadge = (status) => {
    if (status === "APPROVED")
      return (
        <span className="px-2 py-1 bg-green-100 text-green-700 rounded text-[11px] font-bold border border-green-200 uppercase">
          Đã duyệt
        </span>
      );
    return (
      <span className="px-2 py-1 bg-yellow-100 text-yellow-700 rounded text-[11px] font-bold border border-yellow-200 uppercase">
        Chờ duyệt
      </span>
    );
  };

  const getTypeBadge = (type) => {
    const colors = {
      FUEL: "bg-orange-100 text-orange-700",
      REPAIR: "bg-red-100 text-red-700",
      TOLL: "bg-blue-100 text-blue-700",
      OTHER: "bg-gray-100 text-gray-700",
    };
    return (
      <span className={`px-2 py-1 rounded text-xs font-semibold ${colors[type] || "bg-slate-100"}`}>
        {type}
      </span>
    );
  };

  const renderPageNumbers = () => {
    const pages = [];
    let startPage = Math.max(0, pagination.page - 2);
    let endPage = Math.min(pagination.totalPages - 1, pagination.page + 2);
    for (let i = startPage; i <= endPage; i++) {
      pages.push(
        <button
          key={i}
          onClick={() => handlePageChange(i)}
          className={`w-8 h-8 rounded-md text-sm font-medium transition ${
            pagination.page === i ? "bg-blue-600 text-white" : "bg-white border hover:bg-slate-50"
          }`}
        >
          {i + 1}
        </button>
      );
    }
    return pages;
  };
  
  return (
    <div className="bg-white rounded-lg shadow p-6 min-h-[600px] flex flex-col justify-between relative">
      
      {/* HEADER */}
      <div className="flex flex-col md:flex-row justify-between items-center mb-6 gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-800 flex items-center">
            <DollarSign className="mr-2" /> Quản lý Chi phí
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Tổng số: <span className="font-bold text-blue-600">{pagination.total}</span> phiếu chi
          </p>
        </div>
        <div className="flex gap-2">
            <button onClick={openCreateModal} className="flex items-center px-4 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm font-bold transition shadow-lg shadow-blue-200">
                <Plus size={16} className="mr-2" /> Tạo phiếu chi
            </button>
        </div>
      </div>

      {/* TABLE */}
      <div className="overflow-x-auto border border-slate-200 rounded-xl">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="text-slate-500 border-b border-slate-200 text-[11px] uppercase tracking-wider bg-slate-50">
              <th className="px-4 py-3 font-bold">STT</th>
              <th className="px-4 py-3 font-bold">Loại</th>
              <th className="px-4 py-3 font-bold">Số tiền</th>
              <th className="px-4 py-3 font-bold">Ngày phát sinh</th>
              <th className="px-4 py-3 font-bold">Mô tả</th>
              <th className="px-4 py-3 font-bold">Chuyến đi liên quan</th>
              <th className="px-4 py-3 font-bold text-center">Trạng thái</th>
              <th className="px-4 py-3 font-bold text-right">Thao tác</th>
            </tr>
          </thead>
          <tbody className="text-sm text-slate-700 divide-y divide-slate-100">
            {loading ? (
              <tr><td colSpan="8" className="text-center py-10 text-slate-400">Đang tải dữ liệu...</td></tr>
            ) : expenses.length === 0 ? (
              <tr><td colSpan="8" className="text-center py-10 text-slate-400">Chưa có dữ liệu</td></tr>
            ) : (
              expenses.map((item, index) => {
                const travelInfo = getTravelDisplayInfo(item.travelId); // Gọi helper dịch ID
                return (
                  <tr key={item.id || index} className="hover:bg-slate-50 transition-colors group">
                    <td className="px-4 py-3 text-slate-500 font-medium">
                      {((pagination.page || 0) * (pagination.size || 10)) + index + 1}
                    </td>
                    <td className="px-4 py-3">{getTypeBadge(item.type)}</td>
                    <td className="px-4 py-3 font-mono font-bold text-slate-700">
                      {formatCurrency(item.expense)}
                    </td>
                    <td className="px-4 py-3 text-slate-600">
                      {item.incurredDate ? moment(item.incurredDate).format('DD/MM/YYYY') : '-'}
                    </td>
                    <td className="px-4 py-3 max-w-xs truncate text-slate-500" title={item.description}>
                      {item.description || "-"}
                    </td>
                    
                    {/* CỘT CHUYẾN ĐI ĐÃ ĐƯỢC DỊCH SANG TÊN */}
                    <td className="px-4 py-3">
                      <div className="font-semibold text-blue-700 flex items-center gap-1">
                        <MapPin size={14} className="text-blue-500" />
                        {travelInfo.route}
                      </div>
                      <div className="text-[11px] font-bold text-slate-400 mt-0.5 ml-4">
                        {travelInfo.details}
                      </div>
                    </td>
                    
                    <td className="px-4 py-3 text-center">
                      {getApprovalBadge(item.approval || item.status)}
                    </td>
                    <td className="px-4 py-3 text-right">
                      <div className="flex items-center justify-end gap-1 transition-opacity">
                          {(!item.approval || item.approval === 'PENDING_APPROVAL') && (
                              <button onClick={() => handleApproval(item.id)} className="p-1.5 text-green-500 hover:bg-green-50 rounded transition" title="Duyệt">
                                  <CheckCircle size={18} />
                              </button>
                          )}
                          
                          <button onClick={() => openEditModal(item)} className="p-1.5 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded transition">
                              <Edit size={16} />
                          </button>
                          <button onClick={() => handleDelete(item.id)} className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded transition">
                              <Trash2 size={16} />
                          </button>
                      </div>
                    </td>
                  </tr>
                )
              })
            )}
          </tbody>
        </table>
      </div>

      {/* FOOTER PAGINATION */}
      <div className="flex flex-col sm:flex-row justify-between items-center mt-6 pt-4 border-t border-slate-100 gap-4">
        <div className="text-sm text-slate-500">
          Trang <span className="font-semibold text-slate-700">{(pagination.page || 0) + 1}</span> / <span className="font-semibold text-slate-700">{pagination.totalPages || 1}</span>
        </div>
        <div className="flex items-center gap-2">
          <button onClick={() => handlePageChange(pagination.page - 1)} disabled={pagination.page === 0} className="flex items-center px-2 py-1.5 border rounded-md hover:bg-slate-50 disabled:opacity-50">
            <ChevronLeft size={16} />
          </button>
          <div className="hidden sm:flex gap-1">{renderPageNumbers()}</div>
          <button onClick={() => handlePageChange(pagination.page + 1)} disabled={pagination.page >= pagination.totalPages - 1} className="flex items-center px-2 py-1.5 border rounded-md hover:bg-slate-50 disabled:opacity-50">
            <ChevronRight size={16} />
          </button>
        </div>
      </div>

      {/* --- MODAL FORM --- */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-sm p-4 animate-fade-in">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg overflow-hidden">
            <div className="bg-slate-800 px-6 py-4 flex justify-between items-center">
              <h3 className="text-white font-bold tracking-wide">
                {modalMode === 'create' ? 'TẠO PHIẾU CHI MỚI' : 'CẬP NHẬT PHIẾU CHI'}
              </h3>
              <button onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-white transition"><XCircle size={22} /></button>
            </div>
            
            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Loại chi phí</label>
                    <select className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none text-sm"
                      value={formData.type}
                      onChange={(e) => setFormData({...formData, type: e.target.value})}
                    >
                        <option value="FUEL">Nhiên liệu (FUEL)</option>
                        <option value="REPAIR">Sửa chữa (REPAIR)</option>
                        <option value="TOLL">Phí cầu đường (TOLL)</option>
                        <option value="OTHER">Khác (OTHER)</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Ngày phát sinh</label>
                    <input type="date" required
                      className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none text-sm text-slate-600"
                      value={formData.incurredDate}
                      onChange={(e) => setFormData({...formData, incurredDate: e.target.value})}
                    />
                  </div>
              </div>

              <div>
                <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Số tiền (VNĐ) <span className="text-red-500">*</span></label>
                <input type="number" min="0" required
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none font-bold text-slate-700"
                  placeholder="VD: 500000"
                  value={formData.expense}
                  onChange={(e) => setFormData({...formData, expense: e.target.value})}
                />
              </div>

              {/* NÂNG CẤP: DROPDOWN CHỌN CHUYẾN ĐI THAY VÌ NHẬP UUID */}
              <div>
                <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Chọn chuyến đi <span className="text-red-500">*</span></label>
                <div className="relative">
                    <select required
                      className="w-full pl-10 pr-3 py-2.5 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none text-sm appearance-none cursor-pointer"
                      value={formData.travelId}
                      onChange={(e) => setFormData({...formData, travelId: e.target.value})}
                    >
                        <option value="">-- Bấm để chọn chuyến đi liên quan --</option>
                        {travelList.map(t => {
                          const tId = t.travelId || t.id;
                          const route = t.scheduleName || (t.startPlace && t.endPlace ? `${t.startPlace} → ${t.endPlace}` : "Chưa xác định");
                          const plate = t.truckPlate || t.licensePlate || "";
                          const date = t.startDate ? moment(t.startDate).format("DD/MM") : "";
                          
                          return (
                            <option key={tId} value={tId}>
                                {plate} | {route} ({date})
                            </option>
                          )
                        })}
                    </select>
                    <Truck size={18} className="absolute left-3 top-2.5 text-slate-400 pointer-events-none"/>
                </div>
              </div>

              <div>
                <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Mô tả chi tiết</label>
                <textarea rows="3"
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none resize-none text-sm"
                  placeholder="Ghi chú chi tiết lý do chi tiền..."
                  value={formData.description}
                  onChange={(e) => setFormData({...formData, description: e.target.value})}
                ></textarea>
              </div>

              <div className="pt-4 flex justify-end gap-3">
                <button type="button" onClick={() => setIsModalOpen(false)} className="px-5 py-2.5 border border-slate-200 text-slate-600 rounded-lg hover:bg-slate-50 font-bold text-sm">HỦY</button>
                <button type="submit" className="px-5 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-bold text-sm flex items-center shadow-lg shadow-blue-200">
                  <Save size={18} className="mr-2" /> LƯU PHIẾU
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ExpensesPage;