-- ============================================================
-- Clear ALL rate limiter data
-- ============================================================

DELETE FROM rate_limiting;

-- Show result
SELECT 
    COUNT(*) as remaining_entries,
    'Rate limiter cleared successfully!' as status
FROM rate_limiting;
