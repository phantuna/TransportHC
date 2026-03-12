import React, { useEffect, useState, useCallback } from "react";
import {
  User, Phone, Calendar, Shield, Wallet, Edit, XCircle, Save, UserCircle, FileText, Search
} from "lucide-react";
import { toast } from "react-toastify";
import moment from "moment";
import axiosClient from "../api/axiosClient";
import { hasPermission } from "../utils/auth"; 

const AccountsPage = () => {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);

  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    username: "", phone: "", birthday: "",
  });

  const [isPayrollModalOpen, setIsPayrollModalOpen] = useState(false);
  const [payrollData, setPayrollData] = useState(null);
  const [payrollLoading, setPayrollLoading] = useState(false);
  const [payrollMonth, setPayrollMonth] = useState(moment().month() + 1);
  const [payrollYear, setPayrollYear] = useState(moment().year());

  const formatRoleName = (roleStr) => {
    if (!roleStr) return "N/A";
    const cleanName = roleStr.replace("ROLE_", "").replace("R_", "").toUpperCase();
    switch (cleanName) {
      case "DRIVER": return "Tài xế";
      case "ADMIN": return "Quản trị viên";
      case "MANAGER": return "Quản lý";
      case "USER": return "Người dùng";
      default: return cleanName;
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(amount || 0);
  };

  const fetchProfile = useCallback(async () => {
    setLoading(true);
    try {
      const res = await axiosClient.get("/user/driver/view");
      const data = res.data?.result || res.data;
      setProfile(data);
    } catch (error) {
      toast.error("Không thể tải thông tin tài khoản!");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchProfile();
  }, [fetchProfile]);

  const openEditModal = () => {
    if (profile) {
      setFormData({
        username: profile.username || "",
        phone: profile.phone || "",
        birthday: profile.birthday ? moment(profile.birthday).format("YYYY-MM-DD") : "",
      });
    }
    setIsEditModalOpen(true);
  };

  const handleUpdate = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    try {
      const payload = {
        username: formData.username.trim(),
        phone: formData.phone.trim(),
        birthday: formData.birthday ? formData.birthday : null,
      };

      await axiosClient.put(`/user/update`, payload);
      toast.success("Cập nhật thông tin thành công!");
      
      if (payload.username && payload.username !== localStorage.getItem("username")) {
        localStorage.setItem("username", payload.username);
      }
      
      setIsEditModalOpen(false);
      fetchProfile();
    } catch (error) {
      toast.error(error?.response?.data?.message || "Cập nhật thất bại! Vui lòng thử lại.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const fetchMyPayroll = async () => {
    if (!hasPermission("VIEW_PAYROLL")) {
        toast.error("Bạn không có quyền xem bảng lương!");
        return;
    }

    setPayrollLoading(true);
    try {
      const res = await axiosClient.get("/report/payroll/my", {
        params: { month: payrollMonth, year: payrollYear }
      });
      const data = res.data?.result || res.data;
      setPayrollData(data);
    } catch (error) {
      console.error(error);
      setPayrollData(null);
      const errMsg = error?.response?.data?.message || "Không thể tải bảng lương. Vui lòng kiểm tra lại dữ liệu!";
      toast.error(errMsg);
    } finally {
      setPayrollLoading(false);
    }
  };

  const openPayrollModal = () => {
    setIsPayrollModalOpen(true);
    fetchMyPayroll(); 
  };

  if (loading) {
    return (
      <div className="bg-white rounded-xl shadow-sm p-6 min-h-[600px] flex items-center justify-center border border-slate-200">
        <div className="flex flex-col items-center text-slate-400">
          <div className="w-10 h-10 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin mb-4"></div>
          <p className="font-medium animate-pulse">Đang tải hồ sơ...</p>
        </div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="bg-white rounded-xl shadow-sm p-6 min-h-[600px] flex flex-col items-center justify-center border border-slate-200">
        <UserCircle size={64} className="text-slate-300 mb-4" />
        <h2 className="text-xl font-bold text-slate-800">Không tìm thấy thông tin</h2>
        <p className="text-slate-500">Vui lòng đăng nhập lại hoặc liên hệ quản trị viên.</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl shadow-sm p-6 sm:p-8 min-h-[600px] border border-slate-200">
      
      {/* HEADER PAGE */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4 border-b border-slate-100 pb-6">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-800 flex items-center tracking-tight">
            <div className="p-2 bg-blue-600 rounded-lg mr-3 shadow-md shadow-blue-200">
              <User size={24} className="text-white" />
            </div>
            Tài khoản hệ thống
          </h1>
          <p className="text-sm text-slate-500 mt-2 ml-14 font-medium">
            Quản lý thông tin hồ sơ và bảo mật cá nhân
          </p>
        </div>
        <button 
          onClick={openEditModal}
          className="flex items-center px-5 py-2.5 bg-blue-50 text-blue-700 rounded-xl hover:bg-blue-100 text-sm font-bold transition shadow-sm border border-blue-200"
        >
          <Edit size={16} className="mr-2" /> Chỉnh sửa hồ sơ
        </button>
      </div>

      <div className="max-w-4xl mx-auto">
        <div className="bg-slate-50 rounded-2xl p-6 sm:p-10 border border-slate-200 flex flex-col md:flex-row gap-8 items-center md:items-start shadow-inner">
          {/* AVATAR */}
          <div className="flex flex-col items-center text-center">
            <div className="w-32 h-32 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-full flex items-center justify-center text-white text-5xl font-black shadow-lg shadow-blue-200 border-4 border-white">
              {profile.username ? profile.username.substring(0, 2).toUpperCase() : "U"}
            </div>
            <h2 className="text-2xl font-bold text-slate-800 mt-4">{profile.username}</h2>
            <div className="mt-2 flex flex-wrap justify-center gap-1">
              {profile.roleIds && profile.roleIds.length > 0 ? (
                profile.roleIds.map((role, idx) => (
                  <span key={idx} className="px-3 py-1 bg-emerald-100 text-emerald-700 border border-emerald-200 rounded-full text-xs font-bold uppercase tracking-wider">
                    {formatRoleName(role)}
                  </span>
                ))
              ) : (
                <span className="px-3 py-1 bg-slate-200 text-slate-600 rounded-full text-xs font-bold uppercase">TÀI XẾ</span>
              )}
            </div>
          </div>

          {/* THÔNG TIN CHI TIẾT */}
          <div className="flex-1 w-full">
            <h3 className="text-lg font-bold text-slate-800 mb-4 border-b border-slate-200 pb-2">Thông tin liên hệ & Công việc</h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
              <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex items-start gap-3 transition hover:shadow-md">
                <div className="p-2 bg-blue-50 text-blue-600 rounded-lg"><Phone size={20} /></div>
                <div>
                  <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wide">Số điện thoại</p>
                  <p className="text-slate-800 font-medium font-mono mt-0.5">{profile.phone || "Chưa cập nhật"}</p>
                </div>
              </div>

              <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex items-start gap-3 transition hover:shadow-md">
                <div className="p-2 bg-amber-50 text-amber-600 rounded-lg"><Calendar size={20} /></div>
                <div>
                  <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wide">Ngày sinh</p>
                  <p className="text-slate-800 font-medium mt-0.5">{profile.birthday ? moment(profile.birthday).format("DD/MM/YYYY") : "Chưa cập nhật"}</p>
                </div>
              </div>

              <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-col items-start gap-3 transition hover:shadow-md">
                <div className="flex items-start gap-3">
                  <div className="p-2 bg-emerald-50 text-emerald-600 rounded-lg"><Wallet size={20} /></div>
                  <div>
                    <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wide">Lương cơ bản</p>
                    <p className="text-emerald-600 font-bold mt-0.5 font-mono text-lg leading-none">{profile.baseSalary ? formatCurrency(profile.baseSalary) : "0 đ"}</p>
                  </div>
                </div>
                {hasPermission("VIEW_PAYROLL") && (
                  <button onClick={openPayrollModal} className="mt-1 w-full flex justify-center items-center px-4 py-2.5 bg-emerald-600 text-white rounded-lg text-xs font-bold hover:bg-emerald-700 transition shadow-sm active:scale-95">
                    <FileText size={14} className="mr-2"/> XEM CHI TIẾT LƯƠNG
                  </button>
                )}
              </div>

              <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex items-start gap-3 transition hover:shadow-md">
                <div className="p-2 bg-purple-50 text-purple-600 rounded-lg"><Shield size={20} /></div>
                <div className="overflow-hidden w-full">
                  <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wide">ID Hệ thống</p>
                  <p className="text-slate-500 font-medium text-xs mt-1 truncate" title={profile.role}>{profile.id || "N/A"}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* MODAL CẬP NHẬT HỒ SƠ */}
      {isEditModalOpen && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/50 backdrop-blur-sm p-4 animate-in fade-in duration-200">
          <div className="bg-white rounded-3xl shadow-2xl w-full max-w-md overflow-hidden scale-100 animate-in zoom-in-95 duration-200">
            <div className="bg-slate-800 px-6 py-4 flex justify-between items-center">
              <h3 className="text-white font-bold tracking-wide uppercase">Cập nhật hồ sơ</h3>
              <button onClick={() => setIsEditModalOpen(false)} className="text-slate-400 hover:text-white transition"><XCircle size={22} /></button>
            </div>

            <form onSubmit={handleUpdate} className="p-6 space-y-5">
              <div>
                <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Tên đăng nhập / Họ tên <span className="text-red-500">*</span></label>
                <input type="text" required className="w-full px-4 py-2.5 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none transition text-sm text-slate-800" value={formData.username} onChange={(e) => setFormData({ ...formData, username: e.target.value })} />
              </div>
              <div>
                <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Số điện thoại</label>
                <input type="text" className="w-full px-4 py-2.5 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none transition font-mono text-sm text-slate-800" value={formData.phone} onChange={(e) => setFormData({ ...formData, phone: e.target.value })} />
              </div>
              <div>
                <label className="block text-[11px] font-bold text-slate-500 mb-1 uppercase">Ngày sinh</label>
                <input type="date" className="w-full px-4 py-2.5 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none transition text-sm text-slate-800" value={formData.birthday} onChange={(e) => setFormData({ ...formData, birthday: e.target.value })} />
              </div>

              <div className="pt-4 flex justify-end gap-3 border-t border-slate-100 mt-2">
                <button type="button" disabled={isSubmitting} onClick={() => setIsEditModalOpen(false)} className="px-5 py-2.5 border border-slate-200 rounded-xl text-slate-600 font-bold text-sm hover:bg-slate-50 transition">HỦY BỎ</button>
                <button type="submit" disabled={isSubmitting} className="px-5 py-2.5 bg-blue-600 text-white rounded-xl hover:bg-blue-700 font-bold text-sm flex items-center shadow-lg shadow-blue-200 transition">
                  {isSubmitting ? <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div> : <><Save size={18} className="mr-2" /> LƯU THAY ĐỔI</>}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL XEM BẢNG LƯƠNG CÁ NHÂN (CHUẨN HÓA UI TỪ ẢNH) */}
      {isPayrollModalOpen && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4 overflow-y-auto">
          <div className="bg-white rounded-xl w-full max-w-5xl shadow-2xl overflow-hidden my-8 border border-slate-200">
            
            {/* Header Modal Bảng Lương */}
            <div className="bg-slate-800 px-6 py-4 flex justify-between items-center">
              <h3 className="text-white font-bold tracking-wide uppercase flex items-center text-[15px]">
                <FileText size={18} className="mr-2"/> CHI TIẾT BẢNG LƯƠNG CÁ NHÂN
              </h3>
              <button onClick={() => setIsPayrollModalOpen(false)} className="text-slate-400 hover:text-rose-400 transition bg-slate-700 p-1 rounded-full">
                <XCircle size={20} />
              </button>
            </div>

            <div className="p-6 sm:p-8 bg-white">
              
              {/* Filter */}
              <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm mb-6 flex flex-wrap items-end gap-4">
                <div>
                  <label className="block text-[11px] font-bold text-slate-500 mb-1.5 uppercase">Chọn Tháng</label>
                  <select value={payrollMonth} onChange={(e) => setPayrollMonth(e.target.value)} className="w-32 px-3 py-2.5 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none text-sm cursor-pointer font-medium text-slate-700">
                    {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => (<option key={m} value={m}>Tháng {m}</option>))}
                  </select>
                </div>
                <div>
                  <label className="block text-[11px] font-bold text-slate-500 mb-1.5 uppercase">Nhập Năm</label>
                  <input type="number" value={payrollYear} onChange={(e) => setPayrollYear(e.target.value)} className="w-32 px-3 py-2.5 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none text-sm font-medium text-slate-700" />
                </div>
                <button onClick={fetchMyPayroll} className="flex items-center px-6 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-bold text-sm transition shadow-md shadow-blue-200 active:scale-95">
                  <Search size={16} className="mr-2" /> Tra cứu lương
                </button>
              </div>

              {/* Content */}
              {payrollLoading ? (
                <div className="py-24 text-center text-slate-400 bg-slate-50 rounded-xl border border-slate-200">
                  <div className="w-10 h-10 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin mx-auto mb-4"></div>
                  <p className="font-medium animate-pulse">Đang trích xuất dữ liệu lương...</p>
                </div>
              ) : !payrollData ? (
                <div className="py-24 text-center text-slate-500 bg-slate-50 rounded-xl border border-slate-200">
                  <div className="w-20 h-20 bg-white rounded-full flex items-center justify-center mx-auto mb-4 shadow-sm">
                     <Wallet size={40} className="text-slate-300" />
                  </div>
                  <p className="font-bold text-lg text-slate-700">Chưa có dữ liệu</p>
                  <p className="text-sm mt-1">Không tìm thấy thông tin lương của Tháng {payrollMonth}/{payrollYear}</p>
                </div>
              ) : (
                <div className="space-y-6">
                  
                  {/* Khối 1: Thu nhập & Khấu trừ */}
                  <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    {/* Cột 1: Thu nhập */}
                    <div className="bg-white border border-slate-200 rounded-xl overflow-hidden">
                      <div className="bg-emerald-50 px-4 py-3 border-b border-emerald-100 flex items-center">
                         <div className="w-2 h-2 rounded-full bg-emerald-500 mr-2"></div>
                         <h4 className="font-bold text-emerald-800 uppercase text-[11px] tracking-wider">Các Khoản Thu Nhập</h4>
                      </div>
                      <table className="w-full text-[13px]">
                        <tbody className="divide-y divide-slate-100">
                          {payrollData.incomes?.map((inc, i) => (
                            <tr key={i} className="hover:bg-slate-50">
                              <td className="px-4 py-3.5 font-medium text-slate-700">{inc.name}</td>
                              <td className="px-4 py-3.5 text-right font-mono font-bold text-emerald-600 text-[14px]">{formatCurrency(inc.actualAmount)}</td>
                            </tr>
                          ))}
                          {(!payrollData.incomes || payrollData.incomes.length === 0) && (
                            <tr><td colSpan="2" className="p-6 text-center text-slate-400 italic">Không có dữ liệu thu nhập</td></tr>
                          )}
                        </tbody>
                      </table>
                    </div>

                    {/* Cột 2: Khấu trừ */}
                    <div className="bg-white border border-slate-200 rounded-xl overflow-hidden">
                       <div className="bg-rose-50 px-4 py-3 border-b border-rose-100 flex items-center">
                         <div className="w-2 h-2 rounded-full bg-rose-500 mr-2"></div>
                         <h4 className="font-bold text-rose-800 uppercase text-[11px] tracking-wider">Các Khoản Khấu Trừ</h4>
                      </div>
                      <table className="w-full text-[13px]">
                        <tbody className="divide-y divide-slate-100">
                          {payrollData.deductions?.map((ded, i) => (
                            <tr key={i} className="hover:bg-slate-50">
                              <td className="px-4 py-3.5 font-medium text-slate-700">{ded.name}</td>
                              <td className="px-4 py-3.5 text-right font-mono font-bold text-rose-600 text-[14px]">-{formatCurrency(ded.amount)}</td>
                            </tr>
                          ))}
                          {(!payrollData.deductions || payrollData.deductions.length === 0) && (
                            <tr><td colSpan="2" className="p-6 text-center text-slate-400 italic">Không có khoản trừ nào</td></tr>
                          )}
                        </tbody>
                      </table>
                    </div>
                  </div>

                  {/* Khối 2: Lương chuyến */}
                  <div className="bg-white border border-slate-200 rounded-xl overflow-hidden">
                    <div className="bg-blue-50 px-4 py-3 border-b border-blue-100 flex items-center">
                        <div className="w-2 h-2 rounded-full bg-blue-600 mr-2"></div>
                        <h4 className="font-bold text-blue-800 uppercase text-[11px] tracking-wider">Chi tiết Lương chuyến (Vận chuyển)</h4>
                    </div>
                    <div className="overflow-x-auto">
                        <table className="w-full text-[13px] text-left">
                        <thead className="text-slate-500 text-[11px] uppercase bg-white border-b border-slate-100">
                            <tr>
                              <th className="px-5 py-3 font-bold">Lộ trình chuyến đi</th>
                              <th className="px-5 py-3 font-bold text-center">Số chuyến</th>
                              <th className="px-5 py-3 font-bold text-right">Đơn giá chuyến</th>
                              <th className="px-5 py-3 font-bold text-right">Thành tiền</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100">
                            {payrollData.trips?.map((trip, i) => (
                            <tr key={i} className="hover:bg-slate-50 transition-colors">
                                <td className="px-5 py-3.5 text-slate-800 font-bold uppercase">{trip.routeName}</td>
                                <td className="px-5 py-3.5 text-center font-bold text-slate-700">{trip.tripCount}</td>
                                <td className="px-5 py-3.5 text-right font-mono text-slate-500">{formatCurrency(trip.unitPrice)}</td>
                                <td className="px-5 py-3.5 text-right font-mono font-bold text-blue-600 text-[14px]">{formatCurrency(trip.amount)}</td>
                            </tr>
                            ))}
                            {(!payrollData.trips || payrollData.trips.length === 0) && (
                              <tr><td colSpan="4" className="p-8 text-center text-slate-400 italic">Tháng này chưa có dữ liệu chuyến đi nào được duyệt.</td></tr>
                            )}
                            <tr className="bg-white">
                              <td colSpan="3" className="px-5 py-4 text-right text-slate-700 font-bold uppercase text-[12px] tracking-wider border-t border-slate-200">
                                  Tổng Cộng Tiền Lương Chuyến:
                              </td>
                              <td className="px-5 py-4 text-right text-blue-700 font-black text-[16px] font-mono border-t border-slate-200">
                                  {formatCurrency(payrollData.totalTripSalary)}
                              </td>
                            </tr>
                        </tbody>
                        </table>
                    </div>
                  </div>

                  {/* Khối 3: Tổng kết Thực nhận */}
                  <div className="bg-slate-800 rounded-xl p-6 sm:p-8 text-white flex flex-col md:flex-row items-center justify-between shadow-md">
                    <div className="text-center md:text-left mb-4 md:mb-0">
                      <p className="text-slate-300 font-bold uppercase tracking-widest text-[11px] mb-1.5">Tổng kết thu nhập</p>
                      <h2 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">Thực Lãnh Tháng {payrollData.month}/{payrollData.year}</h2>
                      <p className="text-[13px] text-slate-400 mt-2 max-w-md">
                        Đã bao gồm lương cơ bản, tiền công theo chuyến và đã trừ các khoản khấu hao bắt buộc.
                      </p>
                    </div>
                    
                    <div className="bg-slate-900/50 px-6 py-4 rounded-xl border border-slate-700 text-right min-w-[280px]">
                      <p className="text-4xl sm:text-[42px] font-black font-mono tracking-tight text-emerald-400 drop-shadow-sm">
                        {formatCurrency(payrollData.totalSalary)}
                      </p>
                    </div>
                  </div>

                </div>
              )}
            </div>
          </div>
        </div>
      )}

    </div>
  );
};

export default AccountsPage;