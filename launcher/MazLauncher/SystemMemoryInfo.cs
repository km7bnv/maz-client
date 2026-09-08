using System.Runtime.InteropServices;

namespace MazLauncher;

internal static class SystemMemoryInfo
{
    public const int ReservedForSystemMb = 4096;

    public static int GetTotalPhysicalMemoryMb()
    {
        var status = new MemoryStatusEx();
        if (!GlobalMemoryStatusEx(status)) return 0;
        return (int)Math.Min(int.MaxValue, status.TotalPhysical / (1024UL * 1024UL));
    }

    public static int GetRecommendedMinecraftMaximumMb()
    {
        var totalMb = GetTotalPhysicalMemoryMb();
        if (totalMb <= 0) return 4096;

        var availableForMinecraft = totalMb - ReservedForSystemMb;
        if (availableForMinecraft < 1024)
            availableForMinecraft = Math.Max(1024, totalMb / 2);

        // Keep allocations aligned to 512 MB and within the launcher's supported range.
        var aligned = Math.Max(1024, (availableForMinecraft / 512) * 512);
        return Math.Clamp(aligned, 1024, 32768);
    }

    [DllImport("kernel32.dll", SetLastError = true)]
    [return: MarshalAs(UnmanagedType.Bool)]
    private static extern bool GlobalMemoryStatusEx([In, Out] MemoryStatusEx lpBuffer);

    [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Auto)]
    private sealed class MemoryStatusEx
    {
        public uint Length = (uint)Marshal.SizeOf<MemoryStatusEx>();
        public uint MemoryLoad;
        public ulong TotalPhysical;
        public ulong AvailablePhysical;
        public ulong TotalPageFile;
        public ulong AvailablePageFile;
        public ulong TotalVirtual;
        public ulong AvailableVirtual;
        public ulong AvailableExtendedVirtual;
    }
}
