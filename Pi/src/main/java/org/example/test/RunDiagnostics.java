package org.example.test;

/**
 * Run all diagnostic tests
 */
public class RunDiagnostics {
    
    public static void main(String[] args) {
        System.out.println("🏥 RUNNING ALL DIAGNOSTIC TESTS");
        System.out.println("==============================\n");
        
        System.out.println("1️⃣ Database Connection Test:");
        DatabaseDiagnosticTest.main(new String[]{});
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        System.out.println("2️⃣ Specialist Loading Debug:");
        DebugSpecialistLoading.main(new String[]{});
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        System.out.println("3️⃣ Check Existing Specialists:");
        QuickSpecialistTest.main(new String[]{});
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        System.out.println("4️⃣ Final Check:");
        DebugSpecialistLoading.main(new String[]{});
        
        System.out.println("\n🎯 All diagnostics completed!");
        System.out.println("Check the output above to identify the issue.");
    }
}
