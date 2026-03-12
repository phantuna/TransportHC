import React, { useEffect, useState, useCallback } from "react";
import axiosClient from "../api/axiosClient";
import {
  Edit,
  Trash2,
  Plus,
  Upload,
  Download,
  Package,
  Search,
  XCircle,
  Save,
  AlertTriangle,
  ChevronLeft,
  ChevronRight,
  RefreshCw
} from "lucide-react";
import { toast } from "react-toastify";
import moment from "moment";
import { hasPermission } from "../utils/auth";

const InventoryPage = () => {
  const [inventories, setInventories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isSearching, setIsSearching] = useState(false);

  const [items, setItems] = useState([]);
  const [invoices, setInvoices] = useState([]);

  // Pagination State
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    total: 0,
    totalPages: 1,
  });

  // Filter State (Dùng cho API search)
  const [filters, setFilters] = useState({
    keyword: "",
    itemId: "",
    status: "",
    fromDate: "",
    toDate: "",
  });


  // Modal States
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState("create");
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [itemToDelete, setItemToDelete] = useState(null);

  // Import Modal State
  const [isImportModalOpen, setIsImportModalOpen] = useState(false);
  const [importFile, setImportFile] = useState(null);

  const [formData, setFormData] = useState({
    id: "",
    itemId: "",
    quantity: "",
    description: "",
    customerName: "",
    invoiceId: "",
    status: "IMPORT", // Lưu ý: Chỉnh lại value này cho khớp với Enum trong Java của bạn
  });


  const canManage =
    hasPermission("MANAGE_INVENTORY") ||
    hasPermission("ADMIN");

  const canView =
    hasPermission("VIEW_INVENTORY") ||
    canManage;
  // --- 1. LẤY DỮ LIỆU & TÌM KIẾM ---
  const fetchInventories = useCallback(async (page = 0, isSearch = false) => {
    if (!canView) return;

    setLoading(true);

    try {
      if (isSearch) {

        const params = {};

        if (filters.keyword) params.keyword = filters.keyword;
        if (filters.itemId) params.itemId = filters.itemId;
        if (filters.status) params.status = filters.status;

        // BE nhận LocalDate -> KHÔNG thêm T00:00:00
        if (filters.fromDate) params.fromDate = filters.fromDate;
        if (filters.toDate) params.toDate = filters.toDate;

        const res = await axiosClient.get("/inventory/search", { params });

        const listData = res.data || [];

        setInventories(listData);

        setPagination({
          page: 0,
          size: pagination.size,
          total: listData.length,
          totalPages: 1
        });

      } else {

        const res = await axiosClient.get("/inventory/getAll", {
          params: { page, size: pagination.size }
        });

        const data = res.data;

        const listData = data.content || [];

        setInventories(listData);

        setPagination({
          page: data.number,
          size: data.size,
          total: data.totalElements,
          totalPages: data.totalPages
        });

      }

    } catch (err) {
      toast.error("Không thể tải dữ liệu kho");
    } finally {
      setLoading(false);
    }

  }, [filters, pagination.size, canView]);

  useEffect(() => {
    fetchInventories(0, isSearching);
  }, [fetchInventories, isSearching]);

  // --- 2. XỬ LÝ FILTER ---
  const handleFilterChange = (e) => {
    setFilters({ ...filters, [e.target.name]: e.target.value });
  };

  const executeSearch = () => {
    setIsSearching(true);
    fetchInventories(0, true);
  };

  const resetSearch = () => {
    setFilters({
      keyword: "",
      itemId: "",
      status: "",
      fromDate: "",
      toDate: ""
    });

    setIsSearching(false);

    fetchInventories(0, false);
  };

  // --- 3. THÊM / SỬA ---
  const handleSubmit = async (e) => {
    e.preventDefault();
    const payload = {
      itemId: formData.itemId.trim(),
      quantity: Number(formData.quantity),
      description: formData.description,
      customerName: formData.customerName,
      invoiceId: formData.invoiceId || null,
      status: formData.status
    };

    try {
      if (modalMode === "create") {
        await axiosClient.post("/inventory/created", payload);
        toast.success("Thêm phiếu kho thành công!");
      } else {
        await axiosClient.put(`/inventory/updated/${formData.id}`, payload);
        toast.success("Cập nhật phiếu kho thành công!");
      }
      setIsModalOpen(false);
      fetchInventories(modalMode === "create" ? 0 : pagination.page, isSearching);
    } catch (error) {
      toast.error(error?.response?.data?.message || "Thao tác thất bại!");
    }
  };

  // --- 4. XÓA ---
  const confirmDelete = async () => {
    if (!itemToDelete) return;
    try {
      await axiosClient.delete(`/inventory/deleted/${itemToDelete}`);
      toast.success("Xóa phiếu kho thành công!");
      setIsDeleteModalOpen(false);
      setItemToDelete(null);
      fetchInventories(pagination.page, isSearching);
    } catch (error) {
      toast.error(error?.response?.data?.message || "Xóa thất bại!");
    }
  };

  // --- 5. IMPORT / EXPORT ---
  const handleImportSubmit = async (e) => {
    e.preventDefault();
    if (!importFile) return toast.warning("Vui lòng chọn file Excel!");

    const form = new FormData();
    form.append("file", importFile);

    try {
      await axiosClient.post("/inventory/import", form, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      toast.success("Import dữ liệu thành công!");
      setIsImportModalOpen(false);
      setImportFile(null);
      fetchInventories(0, false);
    } catch (error) {
      toast.error("Import thất bại. Kiểm tra định dạng file!");
    }
  };

  const handleExport = async () => {
    try {
      const res = await axiosClient.get("/inventory/export", { responseType: "blob" });
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", "inventory_export.xlsx");
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
      toast.success("Đã tải xuống file Excel!");
    } catch (error) {
      toast.error("Export thất bại!");
    }
  };

  // --- UI HELPER ---
  const openCreateModal = () => {
    setModalMode("create");
    setFormData({ id: "", itemId: "", quantity: "", description: "", customerName: "", invoiceId: "", status: "IMPORT" });
    setIsModalOpen(true);
  };

  const openEditModal = (item) => {

    setModalMode("edit");

    setFormData({
      id: item.inventoryId,
      itemId: item.itemId,
      quantity: item.quantity,
      description: item.description,
      customerName: item.customerName,
      invoiceId: item.invoiceId || "",
      status: item.status
    });

    setIsModalOpen(true);
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case "IMPORT":
        return <span className="px-2.5 py-1 bg-emerald-100 text-emerald-700 rounded text-[10px] font-black border border-emerald-200 uppercase">Nhập kho</span>;
      case "EXPORT":
        return <span className="px-2.5 py-1 bg-rose-100 text-rose-700 rounded text-[10px] font-black border border-rose-200 uppercase">Xuất kho</span>;
      default:
        return <span className="px-2.5 py-1 bg-slate-100 text-slate-600 rounded text-[10px] font-black border border-slate-200 uppercase">{status}</span>;
    }
  };

  useEffect(() => {
    loadItems();
    loadInvoices();
  }, []);

  const loadItems = async () => {
    try {
      const res = await axiosClient.get("/items");
      setItems(res.data || []);
    } catch {
      toast.error("Không tải được danh sách mặt hàng");
    }
  };

  const loadInvoices = async () => {
    try {
      const res = await axiosClient.get("/invoices");
      setInvoices(res.data || []);
    } catch {
      toast.error("Không tải được danh sách hóa đơn");
    }
  };
  const renderPageNumbers = () => {
    const pages = [];
    let startPage = Math.max(0, pagination.page - 2);
    let endPage = Math.min(pagination.totalPages - 1, pagination.page + 2);
    for (let i = startPage; i <= endPage; i++) {
      pages.push(
        <button key={i} onClick={() => fetchInventories(i, isSearching)}
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
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl shadow-sm p-6 min-h-[600px] flex flex-col justify-between border border-slate-200">
      <div>
        {/* HEADER */}
        <div className="flex flex-col md:flex-row justify-between items-center mb-6 gap-4">
          <div>
            <h1 className="text-2xl font-extrabold text-slate-800 flex items-center tracking-tight">
              <div className="p-2 bg-blue-600 rounded-lg mr-3 shadow-md shadow-blue-200"><Package size={24} className="text-white" /></div>
              Quản lý Kho
            </h1>
            <p className="text-sm text-slate-500 mt-2 ml-14 font-medium">
              Đang quản lý <span className="font-bold text-blue-600">{pagination.total}</span> giao dịch
            </p>
          </div>
          <div className="flex gap-2">
            {canManage && (
              <>
                <button onClick={handleExport} className="flex items-center px-4 py-2.5 bg-slate-50 text-slate-700 border border-slate-200 rounded-xl hover:bg-slate-100 text-sm font-bold transition shadow-sm">
                  <Download size={18} className="mr-2" /> Export
                </button>
                <button onClick={() => setIsImportModalOpen(true)} className="flex items-center px-4 py-2.5 bg-emerald-50 text-emerald-700 border border-emerald-200 rounded-xl hover:bg-emerald-100 text-sm font-bold transition shadow-sm">
                  <Upload size={18} className="mr-2" /> Import
                </button>
                <button onClick={openCreateModal} className="flex items-center px-4 py-2.5 bg-blue-600 text-white rounded-xl hover:bg-blue-700 text-sm font-bold transition shadow-lg shadow-blue-200 active:scale-95">
                  <Plus size={18} className="mr-2" /> Tạo mới
                </button>
              </>
            )}
          </div>
        </div>

        {/* BỘ LỌC SEARCH */}
        <div className="bg-slate-50 p-4 rounded-xl border border-slate-200 mb-6 grid grid-cols-1 md:grid-cols-5 gap-4 items-end">
          <div>
            <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Mã hàng / Item ID</label>
            <select
              name="itemId"
              value={filters.itemId}
              onChange={handleFilterChange}
              className="w-full px-3 py-2 border rounded-lg text-sm"
            >
              <option value="">-- Chọn mặt hàng --</option>

              {items.map(item => (
                <option key={item.id} value={item.id}>
                  {item.name} ({item.code})
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Từ khóa</label>
            <input type="text" name="keyword" value={filters.keyword} onChange={handleFilterChange} className="w-full px-3 py-2 border rounded-lg outline-none text-sm" placeholder="Tìm kiếm chung..." />
          </div>
          <div>
            <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Trạng thái</label>
            <select name="status" value={filters.status} onChange={handleFilterChange} className="w-full px-3 py-2 border rounded-lg outline-none text-sm cursor-pointer">
              <option value="">-- Tất cả --</option>
              <option value="IMPORT">Nhập kho (IMPORT)</option>
              <option value="EXPORT">Xuất kho (EXPORT)</option>
            </select>
          </div>
          <div>
            <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Từ Ngày</label>
            <input type="date" name="fromDate" value={filters.fromDate} onChange={handleFilterChange} className="w-full px-3 py-2 border rounded-lg outline-none text-sm" />
          </div>
          <div>
            <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Đến Ngày</label>
            <input type="date" name="toDate" value={filters.toDate} onChange={handleFilterChange} className="w-full px-3 py-2 border rounded-lg outline-none text-sm" />
          </div>
          <div className="grid grid-cols-1 md:grid-cols-5 gap-4 mt-4">
            <div className="md:col-start-2 md:col-span-2 flex gap-3">
              <button onClick={resetSearch} className="flex items-center px-4 py-2 bg-slate-100 text-slate-700 rounded-lg hover:bg-slate-200 font-bold text-sm transition border border-slate-200">
                <RefreshCw size={16} className="mr-2" /> Reset
              </button>
              <button onClick={executeSearch} className="flex items-center px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-bold text-sm transition shadow-md shadow-blue-200">
                <Search size={16} className="mr-2" /> Tìm kiếm
              </button>
            </div>
          </div>
        </div>

        {/* TABLE */}
        <div className="overflow-x-auto border border-slate-200 rounded-xl shadow-sm">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50/80 text-slate-500 text-[11px] uppercase tracking-wider border-b border-slate-200">
                <th className="px-6 py-4 font-bold">STT</th>
                <th className="px-6 py-4 font-bold">Mã Hàng (Item ID)</th>
                <th className="px-6 py-4 font-bold text-right">Số lượng</th>
                <th className="px-6 py-4 font-bold text-center">Trạng thái</th>
                <th className="px-6 py-4 font-bold">Khách hàng</th>
                <th className="px-6 py-4 font-bold">Ngày tạo</th>
                {canManage && <th className="px-6 py-4 font-bold text-right">Thao tác</th>}
              </tr>
            </thead>
            <tbody className="text-sm text-slate-700 divide-y divide-slate-100">
              {loading ? (
                <tr><td colSpan="7" className="text-center py-16 text-slate-400 font-medium animate-pulse">Đang tải dữ liệu...</td></tr>
              ) : inventories.length === 0 ? (
                <tr><td colSpan="7" className="text-center py-16 text-slate-400 font-medium">Không tìm thấy dữ liệu kho</td></tr>
              ) : (
                inventories.map((item, index) => (
                  <tr key={item.inventoryId || item.id || index} className="hover:bg-slate-50/40 transition-colors group">
                    <td className="px-6 py-4 text-slate-400 font-medium">{((pagination.page || 0) * (pagination.size || 10)) + index + 1}</td>
                    <td className="px-6 py-4 font-bold text-slate-800 font-mono">{items.find(i => i.id === item.itemId)?.name || item.itemId}</td>
                    <td className="px-6 py-4 text-right font-bold text-blue-600 text-base">{item.quantity}</td>
                    <td className="px-6 py-4 text-center">{getStatusBadge(item.status)}</td>
                    <td className="px-6 py-4">{item.customerName || "-"}</td>
                    <td className="px-6 py-4 text-slate-500">{item.createdDate ? moment(item.createdDate).format("DD/MM/YYYY") : "-"}</td>
                    {canManage && (
                      <td className="px-6 py-4 text-right">
                        <div className="flex items-center justify-end gap-1  transition-opacity">
                          <button onClick={() => openEditModal(item)} className="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition" title="Sửa">
                            <Edit size={16} />
                          </button>
                          <button onClick={() => { setItemToDelete(item.inventoryId || item.id); setIsDeleteModalOpen(true); }} className="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition" title="Xóa">
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

      {/* FOOTER PAGINATION */}
      {!isSearching && pagination.totalPages > 1 && (
        <div className="flex flex-col sm:flex-row justify-between items-center mt-6 pt-4 border-t border-slate-100 gap-4">
          <div className="text-sm text-slate-500 font-medium">
            Trang <span className="font-semibold text-slate-700">{(pagination.page || 0) + 1}</span> / <span className="font-semibold text-slate-700">{pagination.totalPages || 1}</span>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => !isSearching && fetchInventories(pagination.page - 1)} disabled={pagination.page === 0} className="flex items-center p-1.5 border border-slate-200 rounded-lg text-slate-600 hover:bg-slate-50 disabled:opacity-30 transition">
              <ChevronLeft size={18} />
            </button>
            <div className="hidden sm:flex gap-1">{renderPageNumbers()}</div>
            <button onClick={() => !isSearching && fetchInventories(pagination.page + 1)} disabled={pagination.page >= pagination.totalPages - 1} className="flex items-center p-1.5 border border-slate-200 rounded-lg text-slate-600 hover:bg-slate-50 disabled:opacity-30 transition">
              <ChevronRight size={18} />
            </button>
          </div>
        </div>
      )}

      {/* --- MODAL FORM THÊM/SỬA --- */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-lg overflow-hidden zoom-in duration-200">
            <div className="bg-slate-800 px-6 py-4 flex justify-between items-center">
              <h3 className="text-white font-bold tracking-wide">
                {modalMode === 'create' ? 'TẠO PHIẾU KHO' : 'CẬP NHẬT PHIẾU KHO'}
              </h3>
              <button onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-white transition"><XCircle size={22} /></button>
            </div>

            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Mã hàng (Item ID) <span className="text-red-500">*</span></label>
                  <select
                    required
                    className="w-full px-3 py-2 border rounded-lg text-sm"
                    value={formData.itemId}
                    onChange={(e) =>
                      setFormData({ ...formData, itemId: e.target.value })
                    }
                  >
                    <option value="">-- Chọn mặt hàng --</option>

                    {items.map(item => (
                      <option key={item.id} value={item.id}>
                        {item.name} ({item.code})
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Số lượng <span className="text-red-500">*</span></label>
                  <input type="number" min="1" required
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition text-sm"
                    value={formData.quantity} onChange={(e) => setFormData({ ...formData, quantity: e.target.value })}
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Loại giao dịch <span className="text-red-500">*</span></label>
                  <select required className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition text-sm cursor-pointer"
                    value={formData.status} onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                  >
                    {/* BẠN KIỂM TRA LẠI ENUM JAVA VÀ THAY ĐỔI VALUE CHO KHỚP NHÉ */}
                    <option value="IMPORT">Nhập kho (IMPORT)</option>
                    <option value="EXPORT">Xuất kho (EXPORT)</option>
                  </select>
                </div>
                <div>
                  <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Mã hóa đơn (Tùy chọn)</label>
                  <select
                    className="w-full px-3 py-2 border rounded-lg text-sm"
                    value={formData.invoiceId}
                    onChange={(e) =>
                      setFormData({ ...formData, invoiceId: e.target.value })
                    }
                  >
                    <option value="">-- Không chọn hóa đơn --</option>

                    {invoices.map(inv => (
                      <option key={inv.id} value={inv.id}>
                        {inv.invoiceCode} - {inv.customerName}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Tên khách hàng</label>
                <input type="text"
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition text-sm"
                  placeholder="VD: Cty Cổ phần ABC"
                  value={formData.customerName} onChange={(e) => setFormData({ ...formData, customerName: e.target.value })}
                />
              </div>

              <div>
                <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Mô tả / Ghi chú</label>
                <textarea rows="2"
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition resize-none text-sm"
                  placeholder="Ghi chú..."
                  value={formData.description} onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                />
              </div>

              <div className="pt-4 flex justify-end gap-3">
                <button type="button" onClick={() => setIsModalOpen(false)} className="px-5 py-2 border border-slate-200 rounded-lg text-slate-600 font-bold text-sm hover:bg-slate-50 transition">HỦY</button>
                <button type="submit" className="px-5 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-bold text-sm flex items-center shadow-lg shadow-blue-200 transition">
                  <Save size={18} className="mr-2" /> LƯU LẠI
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* --- MODAL IMPORT EXCEL --- */}
      {isImportModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-md overflow-hidden">
            <div className="bg-emerald-600 px-6 py-4 flex justify-between items-center">
              <h3 className="text-white font-bold tracking-wide">NHẬP DỮ LIỆU EXCEL</h3>
              <button onClick={() => setIsImportModalOpen(false)} className="text-emerald-100 hover:text-white transition"><XCircle size={22} /></button>
            </div>
            <form onSubmit={handleImportSubmit} className="p-6 space-y-4">
              <div className="border-2 border-dashed border-emerald-300 rounded-xl p-8 text-center hover:bg-emerald-50 transition cursor-pointer relative bg-emerald-50/50">
                <input type="file" required accept=".xlsx, .xls"
                  className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                  onChange={(e) => setImportFile(e.target.files[0])}
                />
                <div className="flex flex-col items-center pointer-events-none">
                  <Upload size={36} className="text-emerald-500 mb-3" />
                  <span className="text-sm font-bold text-slate-700">
                    {importFile ? importFile.name : "Kéo thả hoặc chọn file (.xlsx)"}
                  </span>
                  <span className="text-[11px] text-slate-500 mt-1 font-medium">Bấm vào đây để tải tệp lên</span>
                </div>
              </div>
              <div className="pt-2 flex justify-end gap-3">
                <button type="button" onClick={() => setIsImportModalOpen(false)} className="px-5 py-2 border border-slate-200 rounded-lg text-slate-600 font-bold text-sm hover:bg-slate-50 transition">HỦY</button>
                <button type="submit" className="px-5 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 font-bold text-sm shadow-lg shadow-emerald-200 transition">TẢI LÊN</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* --- MODAL XÓA --- */}
      {isDeleteModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-sm p-6 text-center">
            <div className="w-16 h-16 bg-rose-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <AlertTriangle size={32} className="text-rose-600" />
            </div>
            <h3 className="text-xl font-bold text-slate-800 mb-2">Xác nhận xóa</h3>
            <p className="text-slate-500 text-sm mb-6">Bạn có chắc chắn muốn xóa giao dịch kho này không?</p>
            <div className="flex justify-center gap-3">
              <button onClick={() => setIsDeleteModalOpen(false)} className="px-5 py-2 border rounded-lg text-slate-600 font-bold text-sm hover:bg-slate-50">HỦY</button>
              <button onClick={confirmDelete} className="px-5 py-2 bg-rose-600 text-white rounded-lg hover:bg-rose-700 font-bold text-sm shadow-lg shadow-rose-200">XÓA NGAY</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default InventoryPage;