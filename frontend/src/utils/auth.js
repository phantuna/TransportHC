/**
 * Lấy danh sách quyền (permissions) từ LocalStorage
 * @returns {Array<string>} Mảng chứa các quyền
 */
export const getUserPermissions = () => {
  try {
    const permsString = localStorage.getItem("permissions");
    if (!permsString) return [];
    
    const perms = JSON.parse(permsString);
    return Array.isArray(perms) ? perms : [];
  } catch (error) {
    console.error("Lỗi khi đọc permissions từ localStorage:", error);
    return [];
  }
};

/**
 * Kiểm tra xem user hiện tại có một quyền cụ thể nào đó hay không.
 * Nếu user có quyền 'ADMIN' hoặc 'ROLE_ADMIN', hàm sẽ luôn trả về true (Full quyền).
 * * @param {string} permission - Tên quyền cần kiểm tra (VD: 'MANAGE_INVENTORY', 'VIEW_EXPENSE')
 * @returns {boolean} - Trả về true nếu có quyền, ngược lại false
 */
export const hasPermission = (permission) => {
  const perms = getUserPermissions();

  // 1. Nếu mảng quyền rỗng -> Chắc chắn không có quyền
  if (perms.length === 0) return false;

  // 2. [QUAN TRỌNG] BẢO VỆ ADMIN CỦA BẠN 
  // Nếu BE của bạn gán quyền "R_ADMIN" hoặc "ROLE_ADMIN" cho người quản lý tối cao,
  // thì tự động cho phép qua mọi cửa ải check quyền.
  if (perms.includes("R_ADMIN") || perms.includes("ROLE_ADMIN")) {
    return true;
  }

  // 3. Kiểm tra xem quyền truyền vào có nằm trong mảng quyền của user hay không
  return perms.includes(permission);
};

/**
 * Lấy token hiện tại
 */
export const getToken = () => {
  return localStorage.getItem("token");
};

/**
 * Kiểm tra xem người dùng đã đăng nhập chưa
 */
export const isAuthenticated = () => {
  const token = getToken();
  return !!token; // Trả về true nếu có token
};

/**
 * Xóa toàn bộ dữ liệu phiên đăng nhập (Đăng xuất)
 */
export const logout = () => {
  localStorage.removeItem("token");
  localStorage.removeItem("isAuthenticated");
  localStorage.removeItem("username");
  localStorage.removeItem("permissions");
  
  // Tùy chọn: Bắn người dùng về trang chủ
  window.location.href = "/";
};