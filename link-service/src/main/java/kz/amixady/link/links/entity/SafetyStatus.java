package kz.amixady.link.links.entity;

public enum SafetyStatus {
    NOT_CHECKED, // новая или со сменённым адресом, работает как безопасная (F-10.2)
    SAFE,        // проверена, всё чисто
    SUSPICIOUS,  // сработали признаки — страница-предупреждение (F-10.10)
    BLOCKED      // найдена в базах опасных — переход запрещён (F-10.11)
}