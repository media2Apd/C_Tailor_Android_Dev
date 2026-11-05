package com.cuso.tailor.view.home.paywall

import com.cuso.tailor.model.permission.PaywallModuleConfig

object ModuleUpgradeConfig {

    private val configs: Map<String, PaywallModuleConfig> = mapOf(
        // ── Home & Organization Settings ──
        "home" to PaywallModuleConfig(
            moduleKey = "home",
            moduleTitle = "Organization Settings",
            headline = "Upgrade to Unlock Organization Administration",
            description = "Configure company-wide structures, branches, workflows, and administration settings.",
            featuresIncluded = listOf(
                "Organization Profile & Branding",
                "Branch Management",
                "Department & Team Hierarchies",
                "Approval Rules Engine",
                "Warehouse & Storage Setup"
            ),
            efficiencyBadge = "+30% Operational Control",
            heroTitle = "Manage Your Organization",
            heroSubtitle = "Scale your tailoring business with centralized organization administration."
        ),

        // ── Sub-Feature: Organization Profile ──
        "organizationprofile" to PaywallModuleConfig(
            moduleKey = "organizationprofile",
            moduleTitle = "Organization Profile",
            headline = "Upgrade Your Plan to Unlock Organization Profile",
            description = "Upgrade your plan to customize your organization details, branding, business credentials, and portal settings.",
            featuresIncluded = listOf(
                "Company Profile & Logo Setup",
                "Business Registration & Tax Details",
                "Portal & Workspace Configuration",
                "Regional Settings & Timezone"
            ),
            efficiencyBadge = "+25% Brand Trust",
            heroTitle = "Customize Your Profile",
            heroSubtitle = "Build brand trust with custom business details and company branding."
        ),
        // ── Marketing Module ──
        "marketing" to PaywallModuleConfig(
            moduleKey = "marketing",
            moduleTitle = "Marketing Module",
            headline = "Upgrade Your Plan to Unlock Marketing Tools",
            description = "Upgrade your plan to access powerful marketing features that help grow your tailoring business.",
            featuresIncluded = listOf(
                "Campaign Management",
                "Lead Generation",
                "Customer Segmentation",
                "Promotions & Offers",
                "WhatsApp & SMS Messaging",
                "Marketing Reports"
            ),
            efficiencyBadge = "+24% Growth",
            heroTitle = "Ready for Growth?",
            heroSubtitle = "Join 5,000+ tailors who scaled their business using our marketing suite."
        ),

        // ── Sales Module ──
        "sales" to PaywallModuleConfig(
            moduleKey = "sales",
            moduleTitle = "Sales Module",
            headline = "Upgrade Your Plan to Unlock Sales Tools",
            description = "Boost your sales with advanced pipeline management and automation.",
            featuresIncluded = listOf(
                "Sales Pipeline",
                "Deal Tracking",
                "Quotation Builder",
                "Customer Followups",
                "Revenue Forecast",
                "Sales Reports"
            ),
            efficiencyBadge = "+18% Revenue",
            heroTitle = "Increase Your Sales",
            heroSubtitle = "Thousands of businesses improved revenue using our sales tools."
        ),

        // ── HR Module ──
        "hr" to PaywallModuleConfig(
            moduleKey = "hr",
            moduleTitle = "HR Module",
            headline = "Upgrade to Unlock HR Tools",
            description = "Manage employees, payroll, and attendance with advanced HR tools.",
            featuresIncluded = listOf(
                "Employee Management",
                "Attendance Tracking",
                "Payroll Processing",
                "Leave Management",
                "Performance Tracking"
            ),
            efficiencyBadge = "+40% Efficiency",
            heroTitle = "Manage Teams Better",
            heroSubtitle = "HR teams streamline employee management using our HR tools."
        ),

        // ── Inventory Module ──
        "inventory" to PaywallModuleConfig(
            moduleKey = "inventory",
            moduleTitle = "Inventory Module",
            headline = "Upgrade to Unlock Inventory Tools",
            description = "Track stock, suppliers, and warehouse operations easily.",
            featuresIncluded = listOf(
                "Stock Tracking",
                "Warehouse Management",
                "Supplier Management",
                "Low Stock Alerts",
                "Inventory Reports"
            ),
            efficiencyBadge = "+30% Efficiency",
            heroTitle = "Optimize Your Inventory",
            heroSubtitle = "Businesses manage inventory smarter with our tools."
        ),

        // ── Finance Module ──
        "finance" to PaywallModuleConfig(
            moduleKey = "finance",
            moduleTitle = "Finance Module",
            headline = "Upgrade to Unlock Advanced Finance Tools",
            description = "Take full control of your business accounting, from automated tax filing to advanced profit-loss tracking.",
            featuresIncluded = listOf(
                "General Ledger & Accounting",
                "Tax & GST Compliance Tools",
                "Cash Flow Forecasting",
                "Asset & Liability Management",
                "Expense Reimbursements",
                "Financial Audit Logs"
            ),
            efficiencyBadge = "+28% Profit Accuracy",
            heroTitle = "Master Your Money",
            heroSubtitle = "Businesses reduce accounting errors by 40% using our integrated finance suite."
        ),

        // ── IT Management Module ──
        "it" to PaywallModuleConfig(
            moduleKey = "it",
            moduleTitle = "IT Management",
            headline = "Upgrade to Unlock IT Infrastructure Tools",
            description = "Manage your hardware, software licenses, and technical assets with professional IT management tools.",
            featuresIncluded = listOf(
                "IT Asset Tracking",
                "Software License Management",
                "Device Inventory & Health",
                "IT Support Ticket System"
            ),
            efficiencyBadge = "+45% Tech Reliability",
            heroTitle = "Optimize Your Tech",
            heroSubtitle = "Keep your tailoring business running smoothly with centralized IT asset management."
        ),

        // ── Security Module ──
        "security" to PaywallModuleConfig(
            moduleKey = "security",
            moduleTitle = "Security Module",
            headline = "Upgrade to Unlock Enterprise Security",
            description = "Protect your sensitive customer data and business intellectual property with advanced security protocols.",
            featuresIncluded = listOf(
                "Two-Factor Authentication (2FA)",
                "Data Encryption at Rest",
                "Security Audit & Activity Logs",
                "Role-Based Access Control (RBAC)"
            ),
            efficiencyBadge = "100% Data Protection",
            heroTitle = "Secure Your Future",
            heroSubtitle = "Prevent data breaches and unauthorized access with enterprise-grade security tools."
        ),

        // ── Legal & Compliance Module ──
        "legal" to PaywallModuleConfig(
            moduleKey = "legal",
            moduleTitle = "Legal & Compliance",
            headline = "Upgrade to Unlock Legal Management",
            description = "Ensure your tailoring business stays compliant with local laws, digital contracts, and policy management.",
            featuresIncluded = listOf(
                "Digital Contract Templates",
                "Compliance Document Tracking",
                "E-Signatures Integration",
                "Terms & Policy Management"
            ),
            efficiencyBadge = "+100% Policy Compliance",
            heroTitle = "Stay Legally Protected",
            heroSubtitle = "Avoid legal risks by managing professional contracts and compliance in one place."
        ),

        // ── Logistics Module ──
        "logistics" to PaywallModuleConfig(
            moduleKey = "logistics",
            moduleTitle = "Logistics Module",
            headline = "Upgrade to Unlock Logistics Management",
            description = "Streamline your delivery process, track shipments in real-time, and manage courier partners efficiently.",
            featuresIncluded = listOf(
                "Real-time Delivery Tracking",
                "Courier Partner Integration",
                "Shipping Label Generation",
                "Route Optimization"
            ),
            efficiencyBadge = "+22% Faster Delivery",
            heroTitle = "Deliver with Confidence",
            heroSubtitle = "Improve customer satisfaction with accurate delivery timelines and professional tracking."
        ),

        // ── Services Module ──
        "services" to PaywallModuleConfig(
            moduleKey = "services",
            moduleTitle = "Services Module",
            headline = "Upgrade to Unlock Service Management",
            description = "Manage custom service requests, appointments, and client service history with specialized tools.",
            featuresIncluded = listOf(
                "Service Appointment Booking",
                "Custom Order Workflow",
                "Service History Tracking",
                "Client Loyalty Programs"
            ),
            efficiencyBadge = "+40% Client Retention",
            heroTitle = "Deliver Premium Service",
            heroSubtitle = "Build lasting relationships by providing a seamless, digital-first service experience."
        ),

        // ── Reports Module ──
        "reports" to PaywallModuleConfig(
            moduleKey = "reports",
            moduleTitle = "Advanced Reports",
            headline = "Upgrade Your Plan to Unlock Advanced Reporting",
            description = "Get deep insights into your business performance with advanced analytics, custom filters, and automated reporting tools. Upgrade to make data-driven decisions.",
            featuresIncluded = listOf(
                "Custom Report Builder",
                "Financial Profit & Loss Reports",
                "Worker Productivity Analytics",
                "Sales Performance Tracking"
            ),
            efficiencyBadge = "+45% Data Clarity",
            heroTitle = "Make Smarter Decisions",
            heroSubtitle = "Top-performing businesses grow 2x faster by analyzing their key performance metrics daily."
        ),

        // ── Sub-Feature: Requisitions ──
        "requisitionPage" to PaywallModuleConfig(
            moduleKey = "requisitionPage",
            moduleTitle = "Requisition Management",
            headline = "Upgrade Your Plan to Unlock Purchase Requisitions",
            description = "Create and manage purchase requisitions for inventory and procurement workflows. Upgrade your plan to enable requisition management and streamline your purchasing process.",
            featuresIncluded = listOf(
                "Create Purchase Requisitions",
                "Department Approval Workflow",
                "Track Requisition Status",
                "Set Priority for Requests",
                "Link Requisitions with Inventory",
                "Requisition Reports & History"
            ),
            efficiencyBadge = "+35% Procurement Efficiency",
            heroTitle = "Streamline Your Procurement",
            heroSubtitle = "Organizations improve purchasing efficiency by managing requisitions digitally."
        ),

        // ── Sub-Feature: Inventory Dashboard ──
        "inventoryDashboard" to PaywallModuleConfig(
            moduleKey = "inventoryDashboard",
            moduleTitle = "Inventory Dashboard",
            headline = "Upgrade Your Plan to Unlock Inventory Dashboard",
            description = "Get complete visibility into your inventory operations with real-time analytics, stock insights, and performance tracking.",
            featuresIncluded = listOf(
                "Real-time Inventory Overview",
                "Stock Level Monitoring",
                "Low Stock & Out-of-Stock Alerts",
                "Inventory Performance Analytics",
                "Top Moving Products Insights",
                "Inventory Value Tracking"
            ),
            efficiencyBadge = "+40% Inventory Efficiency",
            heroTitle = "Take Control of Your Inventory",
            heroSubtitle = "Businesses optimize stock management using real-time inventory dashboards."
        ),

        // ── Sub-Feature: Suppliers ──
        "suppliers" to PaywallModuleConfig(
            moduleKey = "suppliers",
            moduleTitle = "Supplier Management",
            headline = "Upgrade Your Plan to Unlock Supplier Management",
            description = "Manage all your suppliers in one place. Track supplier details, purchase history, and streamline procurement operations.",
            featuresIncluded = listOf(
                "Supplier Profile Management",
                "Track Supplier Purchase History",
                "Supplier Contact & Address Details",
                "Supplier Performance Tracking",
                "Supplier Payment Tracking",
                "Supplier Reports & Insights"
            ),
            efficiencyBadge = "+25% Procurement Efficiency",
            heroTitle = "Build Strong Supplier Network",
            heroSubtitle = "Businesses improve procurement by managing suppliers in one centralized system."
        ),

        // ── Sub-Feature: Purchase Orders ──
        "purchaseOrders" to PaywallModuleConfig(
            moduleKey = "purchaseOrders",
            moduleTitle = "Purchase Orders",
            headline = "Upgrade Your Plan to Unlock Purchase Orders",
            description = "Create and manage purchase orders easily. Track order status and streamline procurement workflows.",
            featuresIncluded = listOf(
                "Create Purchase Orders",
                "Track Order Status",
                "Approve Purchase Orders",
                "Link Orders with Suppliers",
                "Order History Tracking",
                "Purchase Order Reports"
            ),
            efficiencyBadge = "+32% Purchase Efficiency",
            heroTitle = "Manage Orders Better",
            heroSubtitle = "Businesses streamline procurement using digital purchase order workflows."
        ),

        // ── Sub-Feature: Goods Receives ──
        "purchaseReceives" to PaywallModuleConfig(
            moduleKey = "purchaseReceives",
            moduleTitle = "Goods Receives",
            headline = "Upgrade Your Plan to Unlock Goods Receive Management",
            description = "Track goods received from suppliers and ensure accurate inventory updates.",
            featuresIncluded = listOf(
                "Record Goods Receipts",
                "Match Goods with Purchase Orders",
                "Track Received Quantities",
                "Automatic Inventory Updates",
                "Partial Delivery Tracking",
                "Receive Reports & Logs"
            ),
            efficiencyBadge = "+28% Inventory Accuracy",
            heroTitle = "Track Incoming Goods",
            heroSubtitle = "Businesses ensure accurate stock updates using goods receive tracking."
        ),

        // ── Sub-Feature: Purchase Invoices ──
        "purchaseInvoices" to PaywallModuleConfig(
            moduleKey = "purchaseInvoices",
            moduleTitle = "Purchase Invoices",
            headline = "Upgrade Your Plan to Unlock Purchase Invoices",
            description = "Manage supplier invoices and track payable amounts with ease.",
            featuresIncluded = listOf(
                "Create Supplier Invoices",
                "Link Invoices with Purchase Orders",
                "Track Invoice Status",
                "Due Date Management",
                "Invoice History Tracking",
                "Financial Reports"
            ),
            efficiencyBadge = "+30% Financial Control",
            heroTitle = "Stay on Top of Expenses",
            heroSubtitle = "Businesses manage supplier invoices efficiently with digital invoicing."
        ),

        // ── Sub-Feature: Supplier Payments ──
        "purchasePayables" to PaywallModuleConfig(
            moduleKey = "purchasePayables",
            moduleTitle = "Supplier Payments",
            headline = "Upgrade Your Plan to Unlock Supplier Payments",
            description = "Record and track payments made to suppliers and manage financial transactions easily.",
            featuresIncluded = listOf(
                "Record Supplier Payments",
                "Track Payment Status",
                "Multiple Payment Methods",
                "Payment History Tracking",
                "Outstanding Payables Monitoring",
                "Payment Reports"
            ),
            efficiencyBadge = "+22% Payment Transparency",
            heroTitle = "Track Every Payment",
            heroSubtitle = "Businesses gain better financial visibility with supplier payment tracking."
        ),

        // ── Sub-Feature: Purchase Credits ──
        "purchaseCredits" to PaywallModuleConfig(
            moduleKey = "purchaseCredits",
            moduleTitle = "Purchase Credits",
            headline = "Upgrade Your Plan to Unlock Purchase Credits",
            description = "Manage supplier credit notes for returned or adjusted purchases.",
            featuresIncluded = listOf(
                "Create Supplier Credit Notes",
                "Track Purchase Returns",
                "Adjust Supplier Balances",
                "Link Credits with Invoices",
                "Credit History Tracking",
                "Financial Adjustments Reports"
            ),
            efficiencyBadge = "+18% Financial Accuracy",
            heroTitle = "Handle Returns Easily",
            heroSubtitle = "Businesses manage purchase returns and credits efficiently using credit notes."
        ),

        // ── Sub-Feature: Integrations ──
        "integrations" to PaywallModuleConfig(
            moduleKey = "integrations",
            moduleTitle = "Integration Module",
            headline = "Upgrade to Enable Integrations",
            description = "Connect marketplaces, payment gateways, logistics, and accounting tools by upgrading your plan.",
            featuresIncluded = listOf(
                "Marketplace Integration",
                "Payment Gateway Setup",
                "Shipping Carrier Integration",
                "Accounting Sync"
            ),
            efficiencyBadge = "+50% Automation",
            heroTitle = "Automate Your Business",
            heroSubtitle = "Businesses save hours by automating integrations across platforms."
        )
    )

    /**
     * Resolves the Paywall configuration matching the requested key or module.
     */
    fun getConfig(key: String, planName: String = "Standard Plan"): PaywallModuleConfig {
        val cleanKey = key.trim().lowercase()

        // 1. Direct match (e.g. "sales", "finance", "requisitionpage")
        configs[cleanKey]?.let { return it.copy(currentPlan = planName) }

        // 2. Exact camelCase key match (e.g. "purchaseOrders", "requisitionPage")
        configs[key]?.let { return it.copy(currentPlan = planName) }

        // 3. Fallback generic config
        val capitalized = cleanKey.replaceFirstChar { it.uppercase() }
        return PaywallModuleConfig(
            moduleKey = cleanKey,
            moduleTitle = "$capitalized Module",
            headline = "Upgrade to Unlock $capitalized Tools",
            description = "Upgrade your plan to access all $capitalized features.",
            efficiencyBadge = "+30% Productivity",
            featuresIncluded = listOf(
                "Complete Access to $capitalized",
                "Automated Workflows",
                "Detailed Reports & Insights"
            ),
            heroTitle = "Optimize Your $capitalized",
            heroSubtitle = "Businesses manage $cleanKey smarter with our tools.",
            currentPlan = planName
        )
    }
}